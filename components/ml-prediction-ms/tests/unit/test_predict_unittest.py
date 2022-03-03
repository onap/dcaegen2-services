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
from unittest import mock
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

import requests_mock
from mock import patch

# This method will be used by the mock to replace requests.get
def mocked_requests_get(*args, **kwargs):
    class MockResponse:
        def __init__(self, json_data, status_code):
            self.json_data = json_data
            self.status_code = status_code

        def json(self):
            return self.json_data

    return MockResponse({"key1": "value1"}, 200)

# Our test case class
class ControllerTestCase(unittest.TestCase):

    # We patch 'requests.get' with our own method. The mock object is passed in to our test case method.
    @mock.patch('requests.get', side_effect=mocked_requests_get)
    def test_GetData(self, mock_get):
        # Assert requests.get calls
        ctl = Controller()
        json_data = ctl.GetData()

        self.assertEqual(json_data, {"key1": "value1"})

        # We can even assert that our mocked method was called with the right parameters
        self.assertIn(mock.call(ctl.Config_Object.get_pathToGetData(), verify=False,timeout=3), mock_get.call_args_list)

    def test_simulatedTestDataToReplaceTopic(self):
        self.Controller_Object = Controller()
        status = self.Controller_Object.simulatedTestDataToReplaceTopic()
        
        assert status == False, "Failed"

    def test_PreprocessAndPredict(self):
        ctl = Controller()

        # Opening JSON file
        f = open('tests/unit/sample.json',)
           
        # returns JSON object as 
        # a dictionary
        json_data = json.load(f)        
        
        status = ctl.PreprocessAndPredict(json_data)
        assert status != False


# This method will be used by the mock to replace requests.POST
def mocked_requests_post(*args, **kwargs):
    class MockResponse:
        def __init__(self, json_data, status_code):
            self.json_data = json_data
            self.status_code = status_code

        def json(self):
            return self.json_data

    return MockResponse({"key1": "value1"}, 200)


    #return MockResponse(None, 404)

# Our test case class
class PredictionTestCase(unittest.TestCase):

    # We patch 'requests.get' with our own method. The mock object is passed in to our test case method.
    @mock.patch('requests.post', side_effect=mocked_requests_post)
    def test_Dmaap_url_Exist(self, mock_post):
        # Assert requests.post calls
        pred = Prediction()
        json_data = pred.Dmaap_url_Exist()

        self.assertEqual(json_data, {"key1": "value1"})

        Post_Message ={}
        json_object = json.dumps(Post_Message, indent = 4)

        # We can even assert that our mocked method was called with the right parameters
        self.assertIn(mock.call(pred.Config_Object.get_pathToDmapTopic(), json_object, verify=False, headers={"Content-Type":"application/json"}
            ,timeout=3), mock_post.call_args_list)



class TestPredict(unittest.TestCase):

    def test_Parser(self):
        Controller_Object = Controller(True)
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
    

    def test_Post_Config_Topic(self):
        window_size=4
        self.Predict_Object=Prediction(True)


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

            if status == False:
                break

        assert status == True, "Failed"


    def test_Execute(self):
        self.Controller_Object = Controller(True)
        status = self.Controller_Object.Execute()
        assert bool(status) != False

if __name__ == '__main__':
    unittest.main()
