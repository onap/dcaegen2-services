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
#import input_parser as Data_Parser 
import json 
import time 
import requests 
from requests.auth import HTTPBasicAuth 

import pytest
import src.run

class TestPredict(unittest.TestCase):

	
	
	@pytest.fixture
	def Start_Data_Generation():
		Controller_Object = Controller() 
		return Controller_Object.StartDataGeneration()

	@pytest.fixture
	def Stop_Data_Generate():
		Controller_Object = Controller() 
		Controller_Object.StartDataGeneration()
		return Controller_Object.StopDataGeneration()
		
	def test_Start_Data_Generation(Start_Data_Generation):
		assert Start_Data_Generation == False

	def test_Stop_Data_Generation(Stop_Data_Generation):
		assert Stop_Data_Generation == False
	
	def test_Get_Data(self):
		Controller_Object = src.run.Controller() 
		Controller_Object.StartDataGeneration()
		Controller_Object.StopDataGeneration()
		data = Controller_Object.GetData()
		empty = Controller_Object.GetData()
		self.assertNotEqual(data, empty)

	def test_Preprocess_And_Predict(self):
		Controller_Object = src.run.Controller() 
		Controller_Object.StartDataGeneration()
		Controller_Object.StopDataGeneration()
		data = Controller_Object.GetData()
		status = Controller_Object.PreprocessAndPredict(data)	
		assert status == False

	def test_Predict(self):
		Controller_Object = src.run.Controller() 
		status = Controller_Object.simulatedTestDataToReplaceTopic()		
		assert status == False

	def test_Post_Config_Topic(self):
		df = pd.read_excel('test.xlsx', sheet_name='Sheet1')
		print(df.head())

		data_df=pd.DataFrame()
		
		#wc_df=pd.DataFrame()
		for i  in range(len(df)):
			temp_df=df.iloc[[i]]
			data_df=data_df.append(temp_df)  # parse pm data + configured data + predicted dummy data(=configured data- to be changed after pred)
				
		configured={}
		predicted={}

		for x in range(0,len(data_df.columns),window_size+1): 
			test=data_df.iloc[-5:,x:x+5] 
			cell=test.columns[0].split('_')[0] 
			inv_yhat = self.Predict_Object.Predict_Model(test)  # Predict using model 
			configured[cell]= test.iat[-2,4] 
			inv_yhat = float(inv_yhat[:,-1]) 
			predicted[cell]=inv_yhat 
			#data_df.iloc[[i],[x+4]]=inv_yhat 
			#self.data_dic[data_df.columns[x+4]][-1] = inv_yhat 
			logger.info("predicted data: "+ predicted) 
			updated_predicted= self.Predict_Object.Logic(list(configured.values()), list(predicted.values())) 
			count=0 
			for x in range(0,len(data_df.columns), window_size+1): 
				self.data_dic[data_df.columns[x+4]][-1] =updated_predicted[count] 
				count+=1 
				logger.info('updated',updated_predicted) 
		
		status = Final_Post_Method(predicted, configured, slice_name, server_name)  			 
		assert status == False

if __name__ == '__main__':
	unittest.main()
