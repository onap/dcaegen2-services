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

import pandas as pd
from pandas import DataFrame, read_excel,concat
from numpy import concatenate
from datetime import datetime
import json
import time
import requests
from requests.auth import HTTPBasicAuth
import logging
import configparser
import uuid


# Create and configure logger
import io, os, sys
try:
    # Python 3, open as binary, then wrap in a TextIOWrapper with write-through.
    sys.stdout = io.TextIOWrapper(open(sys.stdout.fileno(), 'wb', 0), write_through=True)
    # If flushing on newlines is sufficient, as of 3.7 you can instead just call:
    sys.stdout.reconfigure(line_buffering=True)
except TypeError:
    # Python 2
    sys.stdout = os.fdopen(sys.stdout.fileno(), 'w', 0)



logging.basicConfig(filename="IntelligentSliceMl.log",
    format='%(asctime)s - %(levelname)s - %(levelno)s - %(process)d - %(name)s  - %(message)s',
                    filemode='w')

# Creating an object
logger = logging.getLogger('ml_ms_prediction')
logger.setLevel(logging.DEBUG)

logger.info("traceID-%s : Start Prediction", str(uuid.uuid4())[:8])
'''
class Config:

    def __init__(self):
        config = configparser.ConfigParser()
        config.readfp(open(r'ml-prediction-ms.config'))

        self.pathToStartPMData = config.get('PM DATA TOPICS', 'PathToStartPMData')
        self.pathToStopPMData = config.get('PM DATA TOPICS', 'PathToStopPMData')
        self.pathToGetData = config.get('PM DATA TOPICS', 'PathToGetData')

        self.pathToGetConfigData = config.get('CONFIG DATA TOPICS', 'PathToGetConfigData')
        self.pathToDmapTopic = config.get('DMAP POLICY UPDATE', 'PathToDmapTopic')

    def get_pathToStartPMData(self):
        return self.pathToStartPMData

    def get_pathToStopPMData(self):
        return self.pathToStopPMData

    def get_pathToGetData(self):
        return self.pathToGetData

    def get_pathToGetConfigData(self):
        return self.pathToGetConfigData

    def get_pathToDmapTopic(self):
        return self.pathToDmapTopic



class Controller:

    def __init__(self):
        self.data_dic={}
        

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
            response = requests.post(self.Config_Object.get_pathToStartPMData(), verify=False)
        except Exception as e:
            status = False
            logger.critical("traceId-%s Error while Start topic to generated data:\n%s", str(uuid.uuid4())[:8], e)

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
            response = requests.post(self.Config_Object.get_pathToStopPMData(), verify=False)
        except Exception as e:
            status = False
            logger.warning("traceId-%s Error while Stop topic on Data Generation:\n%s",str(uuid.uuid4())[:8], e)

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
            response = requests.get(self.Config_Object.get_pathToGetData(), verify=False)
            logger.info('traceId-%s %s', str(uuid.uuid4())[:8],response)
            pm_data = response.json()

        except Exception as e:
            logger.critical("traceId-%s Error while Get Data from topic :\n%s",str(uuid.uuid4())[:8], e)
        return pm_data

    


    def Execute(self, IsUnitetestPath):
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
        try:

            while bExecute:
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

                    

                if IsUnitetestPath == True:
                    bExecute = False

        except Exception as e:
            logger.critical("traceId-%s Error during Prediction start process f:\n%s",str(uuid.uuid4())[:8], e)
            status = False

        return status


if __name__ == "__main__":
    try:

        time.sleep(30)
        logger.info("traceId-%s : Start Prediction",str(uuid.uuid4())[:8])
        Controller_Object = Controller()
        Controller_Object.Execute(False)
        
    except Exception as e:
        logger.critical("traceId-%s Error onStart Prediction Process:\n%s",str(uuid.uuid4())[:8], e)
'''
