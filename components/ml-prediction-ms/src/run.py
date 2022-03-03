# ============LICENSE_START=======================================================  
#  ml-prediction-ms
# ================================================================================  
# Copyright (C) 2022 Wipro Limited  
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

global window_size
window_size=4
 
# Create and configure logger
logging.basicConfig(filename="IntelligentSliceMl.log",
                    format='%(asctime)s %(message)s',
                    filemode='w')
 
# Creating an object
logger = logging.getLogger()

class Parser: 
    def Data_Parser(self, data_val,data_dic,features, slice_name): 
        """  
        Perform Data Parser   
        READ THE ACTUAL PARAMETERS FROM THE topic MESSAGE AND ADDS IT INTO A DICTIONARY   
        Args:  
            cells_data_list: Cell data list object  
            data_dic: The Parsed data on cell data contained in dictionary   
            features: Data featurs (PM Metrics) 
        Returns:  
            data_dic: none  
        Raises:  
            RuntimeError: Error while Process Slices Cells Data.  
        """ 
        try: 
            len_data_val=len(data_val)
            for i in range(len_data_val): 
                cell_id = data_val[i]['measObjInstId'] 
                response = requests.get('http://10.31.4.14:8086/api/sdnc-config-db/v4/nrcellcu-configdata/'+cell_id+'/snssai/01-B989BD') 
                config_data=response.json()['maxNumberOfConns'] 
                logger.info(config_data) 
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
                #so for the first 4 time instant we are generating synthetic data generation for prediction result
                #this is  done as a softmax correction, essential for better accuracy
                #After the first 4 time instance the predicted values are used and and taken forward.
                for j in range(3,5):
                    key = slice_name+'_'+cell_id +features[j]
                    if key not in data_dic:
                        data_dic[key]=[config_data]
                    elif j==3:
                        data_dic[key].append(config_data)
                    elif j==4:
                        change= data_dic[slice_name+'_'+cell_id+'_SM.PDUSessionSetupFail.0'][-1]/data_dic[slice_name+'_'+cell_id+'_SM.PDUSessionSetupReq.01'][-1] - data_dic[slice_name+'_'+cell_id+'_SM.PDUSessionSetupFail.0'][-2]/data_dic[slice_name+'_'+cell_id+'_SM.PDUSessionSetupReq.01'][-2]
                        data_dic[key].append(change*config_data+config_data)
        except Exception as e:           # TO-DO: Check if return happens before of after the exception 
            logger.error("Error in Parser Slices Cells Data:\n%s" % e)  
        return data_dic 

class Prediction: 
    
    # Time Series Prediction using the LSTM Model and appplies the Logic to give the final predicted output     
    modelfile = 'model/best_model.h5'     
    model= load_model(modelfile, compile=False)  

    def series_to_supervised(self, data, n_in=1, n_out=1, dropnan=True): 
        """  
        Convert the timeseries into Forecast series  
        Args:  
        n_in: Input window size for time series to carry previous nth time instance value.  
        n_out: output future n time instance value to be forecasted against the timeseries  
        
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
            logger.error("Error Pre Processing Slices Cells Data f:\n%s" % e)  
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
            logger.error("Error in Prediction:\n%s" % e)  
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
            logger.info('Global_change',Global_change) 
            Final_Pred_Val=[]  
            Percent_Change=[] 
            sum_Pred_thresh_change=0 
            # Rule 1 is applied to compute cell based Min /Max (-25%, 25%) 
            len_Prev_Thresh=len(Prev_Thresh)
            for cell_instance in range(len_Prev_Thresh): 
                if (Current_Thresh[cell_instance]-Prev_Thresh[cell_instance])/Prev_Thresh[cell_instance] > 0.25: 
                    Rule_based_Percent = 0.25 # rule bases total percentage 
                elif (Current_Thresh[cell_instance]-Prev_Thresh[cell_instance])/Prev_Thresh[cell_instance] <-0.25: 
                    Rule_based_Percent = -0.25 
                else: 
                    Rule_based_Percent=(Current_Thresh[cell_instance]-Prev_Thresh[cell_instance])/Prev_Thresh[cell_instance] 
            
                Percent_Change.append(Rule_based_Percent) 
                sum_Pred_thresh_change=sum_Pred_thresh_change+Rule_based_Percent # predicted sum of threshold change for all cells 

                #logger.info(Percent_Change, sum(Percent_Change)) 
            if Global_change <= 0.10: 
                logger.info("Global is within range") 
                for cell_instance in range(len_Prev_Thresh): 
                    Final_Pred_Val.append(Prev_Thresh[cell_instance]+Prev_Thresh[cell_instance]*Percent_Change[cell_instance]) 
            else: 
                Thresh_Rule_2 = []  #Rule 2 - to distribut global threshold to all cells based on only 10% increase in slice 
                extra = 0.1*Sum_Prev_Thresh 

                logger.info('Value to distribute', extra) 

                for i in range(len_Prev_Thresh):
                    new_val = Prev_Thresh[i]+extra*Percent_Change[i]/abs(sum_Pred_thresh_change) 
                    if abs(extra*Percent_Change[i]/abs(sum_Pred_thresh_change))> abs(Percent_Change[i]*Prev_Thresh[i]): 
                        new_val = Prev_Thresh[i]+Prev_Thresh[i]*Percent_Change[i] 
                    Final_Pred_Val.append(new_val) 
        except Exception as e:          
            logger.error("Error in Post_Prediction_Logic:\n%s" % e)  
        return Final_Pred_Val 


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
            Post_Message["s-nssai"]= slices 
            Post_Message['data'] = [] 
            #for server in server_info.keys(): 
            temp_dict_1 = {} 
            temp_dict_1['gNBCUName'] = server_name 
            temp_dict_1['cellCUList'] = [] 
            for key, Predicted_Result in Predicted_Results.items(): 
                #print(key) 
                #if key in server_info[server]: 
                temp_dict_2={} 
                temp_dict_2['cellLocalId'] = key 
                temp_dict_2['configData'] = {} 
                temp_dict_2['configData']['maxNumberofConns'] = Previous_Results[key] 
                temp_dict_2['configData']['predictedMaxNumberofConns'] = Predicted_Result
                now = datetime.now() 
                dt_string = now.strftime("%d/%m/%Y %H:%M:%S") 
                temp_dict_2['configData']['lastUpdatedTS'] = dt_string 
                temp_dict_1['cellCUList'].append(temp_dict_2) 
            Post_Message['data'].append(temp_dict_1) 
            postUrl = 'http://message-router:3904/events/unauthenticated.ML_RESPONSE_TOPIC' 
            #response = requests.post(postUrl, Post_Message) 
            #print(response) 
            logger.info(Post_Message) 
        except Exception as e:          
            status = False
            logger.error("Posting the Final Output:\n%s" % e)  

        return status

class Controller: 
    
    def __init__(self): 
        self.data_dic={} 
        self.Parser_Object=Parser() 
        self.Predict_Object=Prediction()         

    def StartDataGeneration(self):
        """
        start the data generation from ransim topic on PM Data for Intelligenct slicing

        Args:
            none: none        
        Returns:
            status: True on success, False on failure

        Raises:
            RuntimeError: Error while Start topic to generated data.
        """
        status = True
        try:
            #Start topic to generated data  
            response = requests.post('http://10.31.4.14:8081/ransim/api/GenerateIntelligentSlicingPmData', verify=False ,auth=('dcae@dcae.onap.org', 'demo123456!'))   
        except Exception as e:        
            pass
            status = False
            logger.error("Error while Start topic to generated data:\n%s" % e)    

        return status

    def StopDataGeneration(self):
        """
        Stop the data generation from ransim topic on PM Data for Intelligenct slicing

        Args:
            none: none        
        Returns:
            status: True on success, False on failure

        Raises:
            RuntimeError: Error while Stop topic to generated data.
        """    
        status = True
        try:
            #stop topic  to generated data        
            response = requests.post('http://10.31.4.14:8081/ransim/api/stopIntelligentSlicingPmData', verify=False ,auth=('dcae@dcae.onap.org', 'demo123456!'))   
        except Exception as e:       
            status = False
            logger.error("Error while Stop topic on Data Generation:\n%s" % e)    

        return status

    def  GetData(self):
        """
        Get the data generation from ransim topic on PM Data for Intelligenct slicing

        Args:
            none: none        
        Returns:
            pm_data: Slices and cells PM data 

        Raises:
            RuntimeError: Error while Get Data from topic.
        """
        pm_data = []
        try:
            response = requests.get('https://10.31.4.21:30226/events/org.onap.dmaap.mr.PERFORMANCE_MEASUREMENTS/mlms-cg/mlms-cid', verify=False ,auth=('dcae@dcae.onap.org', 'demo123456!')) 
            logger.info(response) 
            pm_data = response.json() 
            logger.info(pm_data) 

        except Exception as e:                    
            logger.error("Error while Get Data from topic :\n%s" % e)
        return pm_data

    def simulatedTestDataToReplaceTopic(self):
        #with pd.ExcelFile('test.xlsx', engine="openpyxl") as excel:  
        #    df = pd.read_excel(excel)  
        status = True  
        try:  
            df = pd.read_excel('tests/test.xlsx', sheet_name='Sheet1')  
            new_columns1=[] 
            #new_columns2=[] 
            len_dfcolumns=len(df.columns)
            for i in range(len_dfcolumns): 
                new_columns1.append('01-B989BD_'+df.columns[i]) 
            df.columns=new_columns1 
            slice_name=df.columns[0].split('.')[0] 
            logger.info('slice', slice_name) 
            data_df=pd.DataFrame()  
            #wc_df=pd.DataFrame()  
            len_df=len(df)
            for i  in range(len_df):  
                temp_df=df.iloc[[i]]  
                data_df=data_df.append(temp_df)  # parse pm data + configured data + predicted dummy data(=configured data- to be changed after pred)  

                if len(data_df)<window_size+1: 
                    continue 

                configured={}  
                predicted={}  
                logger.info(data_df)  
                len_data_dfcol=len(data_df.columns)
                for x in range(0,len_data_dfcol,window_size+1):   
                    test=data_df.iloc[-5:,x:x+5]   
                    cell=test.columns[0].split('_')[1]   
                    inv_yhat = self.Predict_Object.Predict_Model(test)  # Predict using model   
                    configured[cell]= test.iat[-2,4]   
                    inv_yhat = float(inv_yhat[:,-1])   
                    predicted[cell]=inv_yhat   
                    #data_df.iloc[[i],[x+4]]=inv_yhat   
                    #self.data_dic[data_df.columns[x+4]][-1] = inv_yhat   
                    #logger.info("predicted data: "+ predicted)   

                updated_predicted= self.Predict_Object.Logic(list(configured.values()), list(predicted.values()))   
                count=0   
                logger.info(updated_predicted) 
                for x in range(0,len_data_dfcol, window_size+1):   
                    data_df.iloc[[i],[x+4]]= updated_predicted[count] 
                    count+=1   

                #logger.info('updated',updated_predicted)   
                self.Predict_Object.Final_Post_Method(predicted, configured, slice_name, 'cucpserver1')  #hardcoding the server name  
            results=data_df   
            logger.info("Predicted Results:",results)     
            #how long this file will be written              
            results.to_csv(datetime.date.today()+"predicted.csv", index=0)  
        except Exception as e: 
            logger.error("Error while prediction :\n%s" % e)             
            status = False  
        return status


    def PreprocessAndPredict(self,pm_data):
        """
        Preprocess the data and start the prediction for each cell in slice

        Args:
            none: none        
        Returns:
            status: Preprocssing and Prediction status

        Raises:
            RuntimeError: Error while Preprocessing data during prediction.
        """
        status = True
        try:
            len_pm_data=len(pm_data)
            for i in range(len_pm_data): 
                temp_data=json.loads(pm_data[i]) 
                sub_data = temp_data['event']['perf3gppFields']['measDataCollection']['measInfoList'][0] 
                server_name = temp_data['event']['perf3gppFields']['measDataCollection']['measuredEntityDn']  

                features=sub_data['measTypes']['sMeasTypesList'] 
                features.extend(['_maxNumberOfConns.configured', '_maxNumberOfConns.predicted']) 
                slice_name=features[0].split('.')[2] 
                data_val= sub_data['measValuesList']     
                self.data_dic= self.Parser_Object.Data_Parser(data_val,self.data_dic,features,slice_name) 
                data_df=pd.DataFrame(self.data_dic) 

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
                    #data_df.iloc[[i],[x+4]]=inv_yhat   
                    #self.data_dic[data_df.columns[x+4]][-1] = inv_yhat   
                logger.info("predicted data: "+ predicted) 
                updated_predicted= self.Predict_Object.Logic(list(configured.values()), list(predicted.values())) 
                logger.info('updated',updated_predicted)
                count=0 
                for x in range(0,len_data_dfcol, window_size+1): 
                    self.data_dic[data_df.columns[x+4]][-1] =updated_predicted[count] 
                    count+=1 
                
                self.Predict_Object.Final_Post_Method(predicted, configured, slice_name, server_name)           
            if len(data_df)>=window_size+1: 
                results=pd.DataFrame(self.data_dic) 
                #print("Predicted Results:",results)   
                results.to_csv(datetime.date.today()+"predicted.csv", index=0) 

        except Exception as e:
            logger.error("Error while Preprocessing data during prediction :\n%s" % e)
            status = False

        return status


    def Execute(self): 
        try:
            while True:
                self.StartDataGeneration()                  
                time.sleep(15) 

                self.StopDataGeneration()            
                time.sleep(5) 
                while True:
                    pm_data = self.GetData()

                    if pm_data==[]: 
                        # Delay for 1 minute (60 seconds).                    
                        #time.sleep(60)   
                        #print('End of Data for prediction')                  
                        break

                    self.PreprocessAndPredict(pm_data)
        except Exception as e:          
            logger.error("Error during Prediction start process f:\n%s" % e)  


if __name__ == "__main__":  
    try: 
        
        time.sleep(30)  
        logger.info(": Start Prediction")  
        Controller_Object = Controller()         
        Controller_Object.Execute() 
        #Controller_Object.simulatedTestDataToReplaceTopic()
    except Exception as e:          
        logger.error("Error onStart Prediction Process:\n%s" % e)  
