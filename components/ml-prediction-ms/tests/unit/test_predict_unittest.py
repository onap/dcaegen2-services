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

import pytest
from src.run import Parser, Prediction, Controller
import unittest

import requests
import responses

from unittest import TestCase
from mock import patch # for Python >= 3.3 use unittest.mock

import pandas as pd
import numpy as np
from pandas import DataFrame, read_csv, read_excel
from pandas import concat
from tensorflow.keras.models import load_model
from sklearn.preprocessing import MinMaxScaler
from numpy import concatenate
from sklearn.metrics import mean_squared_error
from math import sqrt
from datetime import datetime
import json
import time
import requests
from requests.auth import HTTPBasicAuth

class TestPredict(unittest.TestCase):



    @pytest.fixture
    def Start_Data_Generation():
        self.Controller_Object = Controller()
        return Controller_Object.StartDataGeneration()

    @pytest.fixture
    def Stop_Data_Generate():
        Controller_Object = Controller()
        Controller_Object.StartDataGeneration()
        return Controller_Object.StopDataGeneration()

    def test_Start_Data_Generation(Start_Data_Generation):
        assert bool(Start_Data_Generation()) != False

    def test_Stop_Data_Generation(Stop_Data_Generation):
        assert bool(Stop_Data_Generation()) != False

    @pytest.fixture
    def Get_Data(self):
        Controller_Object = Controller()
        Controller_Object.StartDataGeneration()
        Controller_Object.StopDataGeneration()
        data = Controller_Object.GetData()
        return data

    def test_Get_Data(Get_Data):
        assert Get_Data != {}

    def test_Preprocess_And_Predict(self):
        Controller_Object = Controller()
        Controller_Object.StartDataGeneration()
        Controller_Object.StopDataGeneration()
        data = Controller_Object.GetData()
        status = Controller_Object.PreprocessAndPredict(data)
        assert status == False, "Failed"

    def test_Parser(self):
        Controller_Object = Controller()
        Controller_Object.StartDataGeneration()
        Controller_Object.StopDataGeneration()
        pm_data = Controller_Object.GetData()

        Parser_Object = Parser()
        data_dic={}

        status = False

        len_pm_data=len(pm_data)
        for i in range(len_pm_data):
            temp_data=json.loads(pm_data[i])
            sub_data = temp_data['event']['perf3gppFields']['measDataCollection']['measInfoList'][0]
            server_name = temp_data['event']['perf3gppFields']['measDataCollection']['measuredEntityDn']

            features=sub_data['measTypes']['sMeasTypesList']
            features.extend(['_maxNumberOfConns.configured', '_maxNumberOfConns.predicted'])
            slice_name=features[0].split('.')[2]
            data_val= sub_data['measValuesList']
            data_dic= Parser_Object.Data_Parser(data_val,data_dic,features,slice_name)
            data_df=pd.DataFrame(data_dic)

            if len(data_df)<window_size+1:
                continue
            else:
                status = True

        assert status == False, "Failed"

    def test_Parser(self):
        data_dic={}
        Parser_Object=Parser()
        data_val={}
        features={}
        slice_name=""
        data_dic= Parser_Object.Data_Parser(data_val,data_dic,features,slice_name)
        assert data_dic == {}, "Failed"

    def test_Predict(self):
        self.Controller_Object = Controller()
        status = self.Controller_Object.simulatedTestDataToReplaceTopic()
        assert bool(status) != False

    def test_Post_Config_Topic(self):
        window_size=4
        self.Predict_Object=Prediction()


        df = pd.read_excel('tests/unit/test.xlsx', engine='openpyxl')
        new_columns1=[]
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
            status = self.Predict_Object.Final_Post_Method(predicted, configured, slice_name, 'cucpserver1')  #hardcoding the server name
            assert status == True, "Failed"


    def test_Execute(self):
        self.Controller_Object = Controller()
        status = self.Controller_Object.Execute(True)
        assert bool(status) != False

if __name__ == '__main__':
    unittest.main()
