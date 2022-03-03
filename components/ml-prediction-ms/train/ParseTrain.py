#!/usr/bin/env python3
# LICENSE_START=======================================================
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




import json
import time
import requests
import logging
import numpy as np
import pandas as pd
import configparser

from requests.auth import HTTPBasicAuth
from math import sqrt
from numpy import concatenate
from matplotlib import pyplot
from pandas import DataFrame, read_csv, read_excel
from pandas import concat
from sklearn.preprocessing import MinMaxScaler
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import mean_squared_error
from keras.layers import Dense, LSTM
from keras.models import Sequential
from keras.callbacks import EarlyStopping, ModelCheckpoint, ReduceLROnPlateau


# Create and configure logger
logging.basicConfig(filename="ml-prediction-ms-Training.log",
                    format='%(asctime)s %(message)s',
                    filemode='w')
# Creating an object
logger = logging.getLogger()


config = configparser.ConfigParser()
config.readfp(open(r'ml-prediction-ms.config'))

pathToStartPMData = config.get('PM DATA TOPICS', 'PathToStartPMData')
pathToStopPMData = config.get('PM DATA TOPICS', 'PathToStopPMData')
pathToGetData = config.get('PM DATA TOPICS', 'PathToGetData')

pathToGetConfigData = config.get('CONFIG DATA TOPICS', 'PathToGetConfigData')

def Parser(cells_data_list,data_dic,features, slice_name):

    """
    Perform Data Parser
    READ THE ACTUAL PARAMETERS FROM THE topic MESSAGE AND ADDS IT INTO A DICTIONARY

    Args:
        cells_data_list: Cell data list object
        data_dic: The Parsed data on cell data contained in dictionary
        features: Data featurs
        slice_name: Slice Name

    Returns:
        data_dic: none

    Raises:
        RuntimeError: Error while Process Slices Cells Data.
    """
    try:

        for cells_data in cells_data_list:
            cell_id =cells_data['measObjInstId']
            response = requests.get(pathToGetConfigData + cell_id + '/snssai/'+ slice_name)
            config_data=response.json()['maxNumberOfConns']
            print(config_data)
            results= cells_data['measResults']

            for result in results:
                p=int(result['p'])
                value=int(result['sValue'])
                key = cell_id +'_'+features[p-1].split('-')[0]
                if key not in data_dic:
                    data_dic[key]=[value]
                else:
                    data_dic[key].append(value)

            for j in range(3,5):
                key = cell_id +features[j]
                if key not in data_dic:
                    data_dic[key]=[config_data]
                elif j==3:
                    data_dic[key].append(config_data)
                elif j==4:
                    change = (
                        data_dic[cell_id + "_SM.PDUSessionSetupFail.0"][-1]
                        / data_dic[cell_id + "_SM.PDUSessionSetupReq.01"][-1]
                        - data_dic[cell_id + "_SM.PDUSessionSetupFail.0"][-2]
                        / data_dic[cell_id + "_SM.PDUSessionSetupReq.01"][-2]
                    )
                    data_dic[key].append(change*config_data+config_data)
    except Exception as e:
        logger.error("Error in Parser Slices Cells Data:\n%s" % e)

    return data_dic




def GetSlicesCellsData():
    """
    Start the data simulation topic for configured slices and cells
    The process waits for the data to be simulated. Waits for 2 hour period.
    The topic to stop the data simulation are called.
    The process waits for the data to be simulated. Waits for 2 hour period.

    Args:
        none: none.

    Returns:
        none: none

    Raises:
        RuntimeError: if Topics cannot be retrived.
    """

    try:
        #Start topic to generated data
        response = requests.post(pathToStartPMData, verify=False )
        print(response)
    except Exception as e:
        logger.error("Error while Start topic to generated data:\n%s" % e)


    #Wait for 2 hrs for data generation
    time.sleep(3600*2)

    try:
        #stop topic  to generated data
        response = requests.post(pathToStopPMData, verify=False)
        print(response)
    except Exception as e:
        logger.error("Error while Stop topic on Data Generation:\n%s" % e)

    time.sleep(10)

def ProcessSlicesCellsData():
    """
    Start Process Slices Cells Data

    Args:
        none: none.

    Returns:
        none: none

    Raises:
        RuntimeError: Error while Process Slices Cells Data.
    """

    data_dic={}     # Final data will be stored here

    try:

        #infinite loop, looking for input messages from dmaap, it will run till the topic returns a null and then create csv
        while True:
            response = requests.get(pathToGetData, verify=False)
            data = response.json()
            print(data)

            if data==[]:
                break

            for datum in data:
                sub_data=json.loads(datum)
                sub_data = sub_data['event']['perf3gppFields']['measDataCollection']['measInfoList'][0]
                features=sub_data['measTypes']['sMeasTypesList']
                features.extend(['_maxNumberOfConns.configured', '_maxNumberOfConns.predicted'])
                cells_data_list= sub_data['measValuesList']
                slice_name = features[0].split('.')[2]
                data_dic = Parser(cells_data_list, data_dic, features, slice_name)        #calling the parser method

            time.sleep(1)

        data_df=pd.DataFrame(data_dic)

        #data_df.to_excel('PM_train_data.xlsx', index=0)
        #df=pd.read_excel('Final_train_data_s1.xlsx',engine='openpyxl')

        data_df.columns = [i.split('_')[1] for i in data_df.columns]
        print(data_df.columns)

        #To append all the cells one after the other for training
        for x in range(0,len(data_df.columns),5):
            temp_df = data_df.iloc[:,x:x+5]
            if x==0:
                result= pd.DataFrame(columns=temp_df.columns)
            result = result.append(temp_df, ignore_index=True)

        test_data= result.iloc[int(0.9*len(result)):,:]
        train_data=result.iloc[:int(0.9*len(result)),:]
        train_data.to_csv('Train_Data.csv', index=0)
        test_data.to_csv('Test_Data.csv',index=0)
    except Exception as e:
        logger.error("Error while Process Slices Cells Data:\n%s" % e)



def series_to_supervised(data, n_in=1, n_out=1, dropnan=True):
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

def Train():
    """
    Train the data against the Machine learning model.

    Args:
        none: none.

    Returns:
        none: none

    Raises:
        RuntimeError: if AAI Network Function data cannot be retrieved.
    """
    try:

        #Training data will be read here and will be concatenated here to form 5 columns only.
        #(Will depend on how they will provide training data)
        file_name= "Train_Data.csv"
        value = pd.read_csv(file_name)
        print(value)

        scaler = MinMaxScaler(feature_range=(-1, 1))
        scaled = scaler.fit_transform(value)
        window_size=4
        reframed = series_to_supervised(scaled,window_size, 1)
        value=reframed.values


        train = value[:int(0.75*len(value)), :]   #set the range of train, validation and test data
        val= value[int(0.75*len(value)):int(0.90*len(value)),:]
        test = value[int(0.90*len(value)):, :]
        columns=scaled.shape[1]

        #Divide the data into input and outpu for the model to learn
        train_X, train_y = train[:, :window_size*scaled.shape[1] + scaled.shape[1]-1], train[:, -1]
        val_X, val_y = val[:,:window_size*scaled.shape[1] + scaled.shape[1]-1], val[:,-1]
        test_X, test_y = test[:, :window_size*scaled.shape[1] + scaled.shape[1]-1], test[:, -1]

        #Reshaping the data to feed lstm model
        train_X = train_X.reshape((train_X.shape[0], 1, train_X.shape[1]))
        val_X = val_X.reshape((val_X.shape[0], 1, val_X.shape[1]))
        test_X = test_X.reshape((test_X.shape[0], 1, test_X.shape[1]))

        print(val_X.shape,val_y.shape, test_y.shape)
        #Defining the model block here
        model = Sequential()
        model.add(LSTM(20, input_shape=(train_X.shape[1], train_X.shape[2])))
        model.add(Dense(1))

        checkpoint = ModelCheckpoint('best_model.h5', verbose=1, monitor='val_loss',save_best_only=True, mode='auto')
        model.compile(optimizer='adam', loss='mean_squared_error')

        history = model.fit(train_X, train_y, epochs=250, batch_size=512, callbacks=[checkpoint], validation_data=(val_X, val_y), verbose=2, shuffle=False) #epochs>2k
        # plot history
        best_model.save("best_model.h5")
        #print(history.history.keys())
        pyplot.plot(history.history['loss'], label='train')
        pyplot.plot(history.history['val_loss'], label='test')
        pyplot.legend()
        pyplot.show()


    except Exception as e:
        logger.error("Error during ML Training Process:\n%s" % e)

if __name__ == "__main__":
    try:
        logger.info(": starting to get the Simulated slices cells data")
        GetSlicesCellsData()
        ProcessSlicesCellsData()
        Train()
    except Exception as e:
        logger.error("Error while starting to get data for ML Training Process:\n%s" % e)

