# LICENSE_START=======================================================
#  org.onap.dmaap
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

import sys
from flask import Flask
import requests
import socket    
import tensorflow as tf
import pandas as pd
from datetime import datetime
import sys, traceback
mlclientapp = Flask(__name__)

logging.basicConfig(level=logging.WARNING)


@mlclientapp.route("/test")
def test():    
	#Returns unit test.status

	#Parameters:
	#    none:none

	#Returns:
	#    status_str(str):The string with success or failure on unit test mlclient app.
	try:
		status_str = "Success on test!"
	except Exception as e:
		print("Failed on Test")
	logger.info(status_str)
	return status_str
	
@mlclientapp.route("/interface", methods=['GET'])
def interface():
	# Create a new resource using POST and update new resource using POST for ML_RESPONSE_TOPIC
	postUrl = 'http://dmaap:3904/events/unauthenticated.VES_MEASUREMENT_OUTPUT'
	postData = '{"event": {"commonEventHeader": {"domain": "perf3gpp","eventId": "9e7c7db8-7a51-4bff-94f5-b530296edd7c","sequence": 0,"eventName": "perf3gpp_AcmeNode-Acme_pmMeasResult","sourceName": "oteNB5309","reportingEntityName": "","priority": "Normal","startEpochMicrosec": 1538478000000,"lastEpochMicrosec": 1538478900000,"version": "4.0","vesEventListenerVersion": "7.1","timeZoneOffset": "UTC+05:00"},"perf3gppFields": {"perf3gppFieldsVersion": "1.0","measDataCollection": {"granularityPeriod":1538482500000,"measuredEntityUserName": "","measuredEntityDn": "1","measuredEntitySoftwareVersion": "r0.1","measInfoList": [{"measInfoId": {"sMeasInfoId": "some measInfoId"},"measTypes": {"sMeasTypesList": ["SM.PrbUsedDl.001-00110", "SM.PrbUsedUl.001-00110"]},"measValuesList": [{"measObjInstId": "103593989","suspectFlag": "false","measResults": [{"p": 1,"sValue": "75"}, {"p": 2,"sValue": "84"}]}, {"measObjInstId": "103593999","suspectFlag": "false","measResults": [{"p": 1,"sValue": "90"}, {"p": 2,"sValue": "95"}]}]}]}}}}'

	try:
		print("POST Resonse content for endpoint")
		response = requests.post(postUrl, postData)
		print("POST Resonse content for endpoint : "+postUrl)		

	except Exception as e:	
		print("Failed POST request")
		sys.exit(e)
	logger.info(response)
	logger.info(response.json())

	try:
		#Request Data With GET
		getUrl = 'http://dmaap:3904/events/unauthenticated.VES_MEASUREMENT_OUTPUT/1/1'
		x = requests.get(getUrl)
	except Exception as e:	
		logger.error("Failed GET request: {e}", exc_info=True)
		sys.exit(e)
	
	print("GET Resonse content for endpoint : "+getUrl)
	print(x.content)
	print("GET Resonse json")
	print(x.json())

	# datetime object containing current date and time
	now = datetime.now()
	dt_string = now.strftime("%d/%m/%Y %H:%M:%S")
	print(dt_string)

	# update a new resource using POST for ML_RESPONSE_TOPIC
	postUrlMlTopic = 'http://dmaap:3904/events/unauthenticated.ML_RESPONSE_TOPIC'
	postMlResData = '{"s-nssai":"001-00110" "data":[{"gNBCUName": "cucpserver1","cellCUList":[{"cellLocalId":103593989,"configData":{"maxNumberofConns":"20","predictedMaxNumberofConns":"25","lastUpdatedTS":'+dt_string+'}}]}]}'

	try:
		response = requests.post(postUrlMlTopic, postMlResData)
	except Exception as e:
		print("Failed POST request")
		sys.exit(e)

	print('POST Resonse to ML_RESPONSE_TOPIC : '+postUrlMlTopic)
	print(response)		
	return "POST Resonse to ML_RESPONSE_TOPIC", 200

def IntelligentSlicingAI_Thread(name):  	
	time.sleep(30)
	try:	
		while True:
			getUrl = 'http://dmaap:3904/topics'        
			x = requests.get(getUrl)

			print("Get Topic Response = %d",x.status_code)

			time.sleep(1)
	except Exception as e:
		print("Exception Failed inside thread")
		print("Failed POST request")
		sys.exit(e)

if __name__ == "__main__":
	try:	
		x = threading.Thread(target=IntelligentSlicingAI_Thread, args=(1,))  
		x.start()
		mlclientapp.run(host='0.0.0.0')
	except Exception as e:
		print("Failed at creation of the thread")		