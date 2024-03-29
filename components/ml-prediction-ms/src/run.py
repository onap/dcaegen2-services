#!/usr/bin/env python3
# ============LICENSE_START=======================================================
#  ml-prediction-ms
# ================================================================================
# Copyright (C) 2023 Wipro Limited
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================

import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'  # or any {'0', '1', '2'}

import pandas as pd
from pandas import DataFrame, read_excel,concat
from tensorflow.keras.models import load_model
from sklearn.preprocessing import MinMaxScaler
from numpy import concatenate
from datetime import datetime
import json
import time
import requests
from requests.auth import HTTPBasicAuth
import logging
import configparser
import uuid

from confluent_kafka import Consumer
from confluent_kafka import Producer
import socket

global window_size
window_size=4

# Create and configure logger
import io, os, sys
try:
    # Python 3, open as binary, then wrap in a TextIOWrapper with write-through.
    sys.stdout = io.TextIOWrapper(open(sys.stdout.fileno(), 'wb', 0), write_through=True)
    # If flushing on newlines is sufficient, as of 3.7 you can instead just call:
    sys.stdout.reconfigure(line_buffering=True)
except TypeError:
    # older version of Python
    pass



logging.basicConfig(filename="IntelligentSliceMl.log",
    format='%(asctime)s - %(levelname)s - %(levelno)s - %(process)d - %(name)s  - %(message)s',
                    filemode='w')

# Creating an object
logger = logging.getLogger('ml_ms_prediction')
logger.setLevel(logging.DEBUG)

logger.info("traceID-%s : Start Prediction", str(uuid.uuid4())[:8])

class Config:

    def __init__(self):
        config = configparser.ConfigParser()
        config.readfp(open(r'ml-prediction-ms.config'))

        self.pathToStartPMData = config.get('PM DATA TOPICS', 'PathToStartPMData')
        self.pathToStopPMData = config.get('PM DATA TOPICS', 'PathToStopPMData')
        self.getDataTopic = config.get('PM DATA TOPICS', 'GetDataTopic')

        self.pathToGetConfigData = config.get('CONFIG DATA TOPICS', 'PathToGetConfigData')
        self.getPolicyTopic = config.get('POLICY UPDATE', 'GetPolicyTopic')

        self.serverName = config.get('SERVER NAME', 'ServerName')

    def get_pathToStartPMData(self):
        return self.pathToStartPMData

    def get_pathToStopPMData(self):
        return self.pathToStopPMData

    def get_DataTopic(self):
        return self.getDataTopic

    def get_pathToGetConfigData(self):
        return self.pathToGetConfigData

    def get_PolicyTopic(self):
        return self.getPolicyTopic

    def get_serverName(self):
        return self.serverName

class Parser:

    def __init__(self):
        self.Config_Object=Config()

    def Data_Parser(self, data_val,data_dic,features, slice_name):
        """
        Perform Data Parser
        READ THE ACTUAL PARAMETERS FROM THE topic MESSAGE AND ADDS IT INTO A DICTIONARY
        Args:
            cells_data_list: Cell data list object
            data_dic: The Parsed data on cell data contained in dictionary
            features: Data featurs (PM Metrics)
            slice_name : Slice name
        Returns:
            data_dic: none
        Raises:
            RuntimeError: Error while Process Slices Cells Data.
        """
        try:
            len_data_val=len(data_val)
            for i in range(len_data_val):
                cell_id = data_val[i]['measObjInstId']
                response = requests.get(self.Config_Object.get_pathToGetConfigData() + cell_id + '/snssai/'+ slice_name, timeout=3)
                config_data=response.json()['maxNumberOfConns']

                results= data_val[i]['measResults']
                len_results=len(results)
                for j in range(len_results):
                    p=int(results[j]['p'])
                    value=int(results[j]['sValue'])
                    key = slice_name+'_'+cell_id +'_'+features[p-1].split('-')[0]
                    if key not in data_dic:
                        data_dic[key]=[value]
                    else:
                        data_dic[key].append(value)
                #We are normalising the prediction, we are have the actual prediction starting from the 5th time instance
                #so for the first 4 time instances we are generating synthetic data generation for prediction result
                #this is  done as a softmax correction, essential for better accuracy
                #After the first 4 time instances the predicted values are used and and taken forward.
                for j in range(3,5):
                    key = slice_name+'_'+cell_id +features[j]
                    if key not in data_dic:
                        data_dic[key]=[config_data]
                    elif j==3:
                        data_dic[key].append(config_data)
                    elif j==4:
                        change = (
                            data_dic[slice_name + "_" + cell_id + "_SM.PDUSessionSetupFail.0"][-1]
                            / data_dic[slice_name + "_" + cell_id + "_SM.PDUSessionSetupReq.01"][-1]
                            - data_dic[slice_name + "_" + cell_id + "_SM.PDUSessionSetupFail.0"][-2]
                            / data_dic[slice_name + "_" + cell_id + "_SM.PDUSessionSetupReq.01"][-2]
                        )
                        data_dic[key].append(change*config_data+config_data)
        except Exception as e:
            logger.error("traceID-%s Error in Parser Slices Cells Data:\n%s", str(uuid.uuid4())[:8], e)
        except requests.Timeout as error:
            logger.critical("traceId-%s Timeout from Get CONFIG DATA topic :\n%s",str(uuid.uuid4())[:8], error)
        return data_dic

class Prediction:

    # Time Series Prediction using the LSTM Model and appplies the Logic to give the final predicted output
    modelfile = 'model/best_model.h5'
    model= load_model(modelfile, compile=False)

    def __init__(self):
        self.Config_Object=Config()

    def IsPolicyUpdate_url_Exist(self):
        """
        Get the status of the Policy response topic by checking its execution status for unit test module.

        Args:
            none: none
        Returns:
            status:Bool status of Topic

        """
        status = True
        try:
            Post_Message ={}
            json_object = json.dumps(Post_Message, indent = 4)
            conf = {'bootstrap.servers': "kafka:9092",'client.id': socket.gethostname()}

            producer = Producer(conf)
            producer.produce(self.Config_Object.get_PolicyTopic(), value=json_object.encode('utf-8'))
            producer.poll(1)
        except Exception as e:
            status = False
        except requests.Timeout as error:
            status = False
        return status

    def series_to_supervised(self, data, n_in=1, n_out=1, dropnan=True):
        """
        Convert the timeseries into Forecast series
        Args:
        data: Input time series data to be processed
        n_in: Input window size for time series to carry previous nth time instance value.
        n_out: output future n time instance value to be forecasted against the timeseries
        dropnan : Flag to drop nan values

        Returns:
        agg (list): Aggregated list of past time series inputs as per the input window and the time series of the predicted future time instance.

        Raises:
        RuntimeError: Error Pre Processing Slices Cells Data.
        """
        try:
            n_vars = 1 if type(data) is list else data.shape[1]
            df = DataFrame(data)
            cols, names = [],[]
            # input sequence (t-n, ... t-1)
            for i in range(n_in, 0, -1):
                cols.append(df.shift(i))
                names += [('var%d(t-%d)' % (j+1, i)) for j in range(n_vars)]
                # forecast sequence (t, t+1, ... t+n)
                for i in range(0, n_out):
                    cols.append(df.shift(-i))
                    if i == 0:
                        names += [('var%d(t)' % (j+1)) for j in range(n_vars)]
                    else:
                        names += [('var%d(t+%d)' % (j+1, i)) for j in range(n_vars)]
                    # put it all together
                    agg = concat(cols, axis=1)
                    agg.columns = names

            # drop rows with NaN values
            if dropnan:
                agg.dropna(inplace=True)

        except Exception as e:
            logger.error("traceID-%s Error Pre Processing Slices Cells Data f:\n%s",str(uuid.uuid4())[:8], e)
        return agg

    def Predict_Model(self, test):
        """
        Does the Actual Prediction on the Input data

        Args:
            test: Input data to model with current and last 4 time instances (window_size)
        Returns:
            inv_yhat= A 2-D list with predicted results.
        Raises:
            RuntimeError: Error in Prediction.
        """

        inv_yhat =[]

        try:
            scaler = MinMaxScaler(feature_range=(-1, 1))
            scaled = scaler.fit_transform(test)
            reframed = self.series_to_supervised(scaled,window_size, 1)
            test=reframed.values
            test_X= test[:, :window_size*scaled.shape[1] + scaled.shape[1]-1]
            test_X = test_X.reshape((test_X.shape[0], 1, test_X.shape[1]))
            yhat = self.model.predict(test_X)
            test_X = test_X.reshape((test_X.shape[0], test_X.shape[2]))
            inv_yhat = concatenate((test_X[:,-4:],yhat), axis=1)
            inv_yhat = scaler.inverse_transform(inv_yhat)
        except Exception as e:
            logger.critical("traceId-%s Error in Prediction:\n%s",str(uuid.uuid4())[:8], e)
        return inv_yhat

    def Logic(self, Prev_Thresh,Current_Thresh):
        """
        Post prediction, Applies the post processing Logic i.e
        (+-)10% cap on Slice config and (+-)25% on cell config against the predicted values

        Args:
            Prev_Thresh: List of Previous Configuration('maxumberOfConns') as read from config DB
            Current_Thresh: List of Predicted Configuration('maxumberOfConns')
        Returns:
            Final_Pred_Val= List of Final Configuration('maxumberOfConns')
        Raises:
            RuntimeError: Error in Prediction.
        """
        try:
            Sum_Prev_Thresh=sum(Prev_Thresh)
            Sum_Pred_Thresh= sum(Current_Thresh)
            Global_change= abs(sum(Current_Thresh)-sum(Prev_Thresh))/sum(Prev_Thresh)
            #logger.info('Global_change',Global_change)
            Final_Pred_Val=[]
            Percent_Change=[]
            sum_Pred_thresh_change=0
            # Rule 1 is applied to compute cell based Min /Max (-25%, 25%)
            len_Prev_Thresh=len(Prev_Thresh)
            for cell_instance, prev_t in enumerate(Prev_Thresh):
                if (Current_Thresh[cell_instance]-prev_t)/prev_t > 0.25:
                    Rule_based_Percent = 0.25 # rule bases total percentage
                elif (Current_Thresh[cell_instance]-prev_t)/prev_t <-0.25:
                    Rule_based_Percent = -0.25
                else:
                    Rule_based_Percent=(Current_Thresh[cell_instance]-prev_t)/prev_t

                Percent_Change.append(Rule_based_Percent)
                # predicted sum of threshold change for all cells
                sum_Pred_thresh_change=sum_Pred_thresh_change+Rule_based_Percent


            if Global_change <= 0.10:
                for cell_instance, prev_t in enumerate(Prev_Thresh):
                    Final_Pred_Val.append(prev_t+prev_t*Percent_Change[cell_instance])
            else:
                #Rule 2 - to distribut global threshold to all cells based on only 10% increase in slice
                Thresh_Rule_2 = []
                extra = 0.1*Sum_Prev_Thresh

                for i in range(len_Prev_Thresh):
                    new_val = Prev_Thresh[i]+extra*Percent_Change[i]/abs(sum_Pred_thresh_change)
                    if abs(extra*Percent_Change[i]/abs(sum_Pred_thresh_change))> abs(Percent_Change[i]*Prev_Thresh[i]):
                        new_val = Prev_Thresh[i]+Prev_Thresh[i]*Percent_Change[i]
                    Final_Pred_Val.append(new_val)
        except Exception as e:
            logger.error("traceId-%s Error in Post_Prediction_Logic:\n%s", str(uuid.uuid4())[:8], e)
        return Final_Pred_Val

    def acked(err, msg):
        """
        Function to format the error in case of exception being None

        Args:
            err: Exception object
            msg: Error message
        Returns:
            None: None

        """
        if err is not None:
            logger.error("traceId-%s Failed to deliver message: %s",str(uuid.uuid4())[:8], str(err))
        else:
            logger.info('traceId-%s %s',str(uuid.uuid4())[:8], (str(msg)))


    def Final_Post_Method(self, Predicted_Results, Previous_Results, slices, server_name):
        """
        Posts the final predicted output (Final Output of the ML Service)
        Args:
            Predicted_Results: Contains Predicted results w.r.t the cell ID
            Previous_Results: Contains Previous Configured values w.r.t the cell ID
            slices: Slice name
            Server_name: contains server name
        Returns:
            status: True on post with content success, False on post failure.
        Raises:
            RuntimeError: Error Posting the Final Output
        """
        status = True
        try:
            Post_Message ={}
            Post_Message["snssai"]= slices
            Post_Message['data'] = []
            #for server in server_info.keys():
            temp_dict_1 = {}
            temp_dict_1['gNBCUName'] = server_name
            temp_dict_1['cellCUList'] = []
            for key, Predicted_Result in Predicted_Results.items():
                temp_dict_2={}
                temp_dict_2['cellLocalId'] = key
                temp_dict_2['configData'] = {}
                temp_dict_2['configData']['maxNumberofConns'] = int(Previous_Results[key])
                temp_dict_2['configData']['predictedMaxNumberofConns'] = int(Predicted_Result)
                now = datetime.now()
                dt_string = now.strftime("%d/%m/%Y %H:%M:%S")
                temp_dict_2['configData']['lastUpdatedTS'] = dt_string
                temp_dict_1['cellCUList'].append(temp_dict_2)
            Post_Message['data'].append(temp_dict_1)

            json_object = json.dumps(Post_Message, indent = 4)
            response = []

            try:
                conf = {'bootstrap.servers': "kafka:9092",'client.id': socket.gethostname()}
                producer = Producer(conf)
                producer.produce(self.Config_Object.get_PolicyTopic(), value=json_object.encode('utf-8'))

                producer.poll(1)

            except requests.Timeout as error:
                status = False
                logger.error("traceId-%s Posting the Final Output To Dmap Topic:\n%s",str(uuid.uuid4())[:8], error)
                #print(response)
            logger.info('traceId-%s %s',str(uuid.uuid4())[:8], Post_Message)
        except Exception as e:
            status = False
            logger.error("traceId-%s Posting the Final Output:\n%s",str(uuid.uuid4())[:8], e)

        return status

class Controller:

    def __init__(self):
        self.data_dic={}
        self.Parser_Object=Parser()
        self.Predict_Object=Prediction()
        self.Config_Object=Config()


    def  GetData(self, consumer):
        """
        Get the data generation from ransim topic on PM Data for Intelligenct slicing

        Args:
            consumer: Consumer Topic instance to get the PM Data
        Returns:
            pm_data: Slices and cells PM data

        Raises:
            RuntimeError: Error while Get Data from topic.
        """
        pm_data = []
        try:
            msg = consumer.poll(timeout=-1)

            if msg is None:
                # Initial message consumption may take up to
                # `session.timeout.ms` for the consumer group to
                # rebalance and start consuming
                #print("Waiting...")
                logger.info("traceId-%s PM Get Data from topic Waiting...: ",str(uuid.uuid4())[:8])
            elif msg.error():
                #print("ERROR: %s".format(msg.error()))
                logger.critical("traceId-%s Error while PM Get Data from topic :\n%s",str(uuid.uuid4())[:8], msg.error())
            else:
                # Extract the (optional) key and value, and print.
                #print('Received message: {}'.format(msg.value().decode('utf-8')))

                pm_data = msg.value().decode('utf-8')


        except Exception as e:
            logger.critical("traceId-%s Error while Get Data from topic :\n%s",str(uuid.uuid4())[:8], e)
            pm_data = []
        except requests.Timeout as error:
            logger.critical("traceId-%s Timeout from Get Data topic :\n%s",str(uuid.uuid4())[:8], error)
            pm_data = []
        return pm_data

    def simulatedTestDataToReplaceTopic(self):
        """
        Simulate the test PM data and simulate required time seriess data and performs Predcition. 
        This function help to quickly check Tensorflow the environment,
        that is used in the prediction and Training process. 

        Args:
            none: none
        Returns:
            status: Execution status on predcition task

        Raises:
            RuntimeError: Error while executing prediction task.
        """

        #with pd.ExcelFile('test.xlsx', engine="openpyxl") as excel:
        #    df = pd.read_excel(excel)
        status = True

        try:
            df = pd.read_excel('tests/unit/test.xlsx', engine='openpyxl')

            new_columns1=[]
            serverName = self.Config_Object.get_serverName()
            #new_columns2=[]
            len_dfcolumns=len(df.columns)
            for i in range(len_dfcolumns):
                new_columns1.append('01-B989BD_'+df.columns[i])
            df.columns=new_columns1
            slice_name=df.columns[0].split('.')[0]
            data_df=pd.DataFrame()
            len_df=len(df)
            for i  in range(len_df-1):
                temp_df=df.iloc[[i]]
                data_df=data_df.append(temp_df)
                # parse pm data + configured data + predicted dummy data(=configured data- to be changed after pred)
                if len(data_df)<window_size+1:
                    continue

                configured={}
                predicted={}
                len_data_dfcol=len(data_df.columns)
                for x in range(0,len_data_dfcol,window_size+1):
                    test=data_df.iloc[-5:,x:x+5]
                    cell=test.columns[0].split('_')[1]
                    inv_yhat = self.Predict_Object.Predict_Model(test)  # Predict using model
                    configured[cell]= test.iat[-2,4]
                    inv_yhat = float(inv_yhat[:,-1])
                    predicted[cell]=inv_yhat
                updated_predicted= self.Predict_Object.Logic(list(configured.values()), list(predicted.values()))
                count=0
                for x in range(0,len_data_dfcol, window_size+1):
                    data_df.iloc[[i],[x+4]]= updated_predicted[count]
                    count+=1
                ret = self.Predict_Object.Final_Post_Method(predicted, configured, slice_name, serverName)
                if(ret == False):
                    status = False
                    break


            results=data_df
            logger.info("traceId-%s predicted data: ",str(uuid.uuid4())[:8])
            logger.debug(predicted)
            dateTimeObj = datetime.now()
            timestampStr = dateTimeObj.strftime("%d-%b-%Y-%H-%M-%S")
            #print('Current Timestamp : ', timestampStr)


            results.to_csv(timestampStr+"predicted.csv", index=0)
        except Exception as e:
            logger.critical("traceId-%s Error while prediction :\n%s",str(uuid.uuid4())[:8], e)
            status = False
        return status


    def PreprocessAndPredict(self,pm_data):
        """
        Preprocess the data and start the prediction for each slices

        Args:
            pm_data: Performance Monitoring Data of all slices and cells collection
        Returns:
            status: Preprocssing and Prediction status

        Raises:
            RuntimeError: Error while Preprocessing data during prediction.
        """
        status = True
        try:
            len_pm_data=len(pm_data)
            temp_data=json.loads(pm_data[len_pm_data-1])
            sub_data1 = temp_data['event']['perf3gppFields']['measDataCollection']['measInfoList']

            len_sub_data=len(sub_data1)
            for r in range(len_sub_data):
                data_dic1={}
                server_name = temp_data['event']['perf3gppFields']['measDataCollection']['measuredEntityDn']
                sub_data = temp_data['event']['perf3gppFields']['measDataCollection']['measInfoList'][r]

                features=sub_data['measTypes']['sMeasTypesList']

                features.extend(['_maxNumberOfConns.configured', '_maxNumberOfConns.predicted'])

                slice_name=features[0].split('.')[2]
                data_val= sub_data['measValuesList']

                data_dic1= self.Parser_Object.Data_Parser(data_val,data_dic1,features,slice_name)

                #data_df=pd.DataFrame(self.data_dic)
                data_df=pd.DataFrame(data_dic1)

                predic_res=self.predictionandresults(data_df)
        except Exception as e:
            logger.critical("traceId-%s Error while Preprocessing data during prediction :\n%s",str(uuid.uuid4())[:8], e)

        return status



    def predictionandresults(self,data_df):
        """
        Process the data and start the prediction for each cell in slice

        Args:
            pm_data: Performance Monitoring Data of a slices and cells collection
        Returns:
            status: Prediction status

        Raises:
            RuntimeError: Error while prediction.
        """
        status = True
        try:
            data_df1=pd.DataFrame()
            slice_name=data_df.columns[0].split('.')[0]
            len_data_df=len(data_df)
            print('lendatadf:',len_data_df)
            for i in range(len_data_df):
                temp_df=data_df.iloc[[i]]
                data_df1=data_df1.append(temp_df)
                if len(data_df1)<window_size+1:
                    continue

                configured={}
                predicted={}
                len_data_dfcol=len(data_df.columns)
                for x in range(0,len_data_dfcol,window_size+1):
                    test=data_df1.iloc[-5:,x:x+5]
                    cell=test.columns[0].split('_')[1]
                    inv_yhat = self.Predict_Object.Predict_Model(test)  # Predict using model
                    configured[cell]= float(test.iat[-2,4])
                    inv_yhat = float(inv_yhat[:,-1])
                    predicted[cell]=inv_yhat
                    #data_df.iloc[[i],[x+4]]=inv_yhat
                    #self.data_dic[data_df.columns[x+4]][-1] = inv_yhat
                #logger.info("predicted data: "+ predicted)
                logger.info("traceId-%s SUCCESS predicted data: ",str(uuid.uuid4())[:8])
                logger.debug( predicted)
                updated_predicted= self.Predict_Object.Logic(list(configured.values()), list(predicted.values()))
                #logger.info('updated',updated_predicted)
                logger.info('traceId-%s SUCCESS updated:', str(uuid.uuid4())[:8])
                logger.debug(updated_predicted)
                count=0
                for x in range(0,len_data_dfcol, window_size+1):

                    data_df1.iloc[[i],[x+4]]= updated_predicted[count]
                    count+=1

                    self.Predict_Object.Final_Post_Method(predicted, configured, slice_name, server_name)


            if len(data_df)>=window_size+1:
                results=pd.DataFrame(self.data_dic)
                #print("Predicted Results:",results)
                dateTimeObj = datetime.now()
                timestampStr = dateTimeObj.strftime("%d-%b-%Y-%H-%M-%S")
                #print('Current Timestamp : ', timestampStr)
                results.to_csv(timestampStr+"predicted.csv", index=0)
        except Exception as e:
            logger.critical("traceId-%s Error while Preprocessing data during prediction :\n%s",str(uuid.uuid4())[:8], e)
            status = False

        return status


    def Execute(self):
        """
        Executes workflow of task methods to get data from topics, then performs preprocessng and Prediction.

        Args:
            none: none
        Returns:
            none: none

        Raises:
            RuntimeError: Error during Prediction start process.
        """
        status = True
        bExecute = True
        pm_data = []
        try:
            conf = {'bootstrap.servers': "kafka:9092",'group.id': "1",'auto.offset.reset': 'smallest'}

            consumer = Consumer(conf)
            consumer.subscribe(([self.Config_Object.get_DataTopic(), -1]))

            while bExecute:
                #self.StartDataGeneration()
                #time.sleep(15)

                #self.StopDataGeneration()
                #time.sleep(5)
                while True:
                    pm_data = self.GetData(consumer)

                    if pm_data==[]:
                        # Delay for 1 minute (60 seconds)
                        #time.sleep(60)
                        break

                    self.PreprocessAndPredict(pm_data)


        except Exception as e:
            logger.critical("traceId-%s Error during Prediction start process f:\n%s",str(uuid.uuid4())[:8], e)
            status = False

        return status


if __name__ == "__main__":
    try:

        time.sleep(60)
        logger.info("traceId-%s : Start Prediction",str(uuid.uuid4())[:8])
        Controller_Object = Controller()
        Controller_Object.Execute()
        #unit test code
        #Controller_Object.simulatedTestDataToReplaceTopic()
    except Exception as e:
        logger.critical("traceId-%s Error onStart Prediction Process:\n%s",str(uuid.uuid4())[:8], e)

