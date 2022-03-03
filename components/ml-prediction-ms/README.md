# LICENSE_START=======================================================
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

This Project aims at training an ML model to predict the 'maxNumberOfConns' for each cell. The model is trained against the 'Performance Measurement' data to predict the output. This model will be later used to predict the real time configurations of cells and slices in ml-prediction-ms.

We are using a stacked LSTM model to perform predictions of the slices and cell configurations.

Assumption
----------
Training module is a offline code, this is not part of the ml-prediction-ms

Prerequisites to run both prediction module and Training code:
-------------------------------------------------------------
1. Training module requires following RANSim Topics reference at 
   https://wiki.onap.org/plugins/servlet/mobile?contentId=1015829#content/view/93002871

   -'http://ransim:8081/ransim/api/GenerateIntelligentSlicingPmData'- Start data generation.
   -'http://ransim:8081/ransim/api/stopIntelligentSlicingPmData'- Stop data generation.
   -'http://message-router:3904/events/unauthenticated.PERFORMANCE_MEASUREMENTS/mlms-cg/mlms-cid'- To receive the PM data (3 feature vectors). If there's any error, check and restart the code.

   ConfigDb application reference https://wiki.onap.org/plugins/servlet/mobile?contentId=1015829#content/view/93002873
   -'http://10.31.4.14:8086/api/sdnc-config-db/v4/nrcellcu-configdata/'+cell_id+'/snssai/01-B989BD' - To get the saved config details (For 4th and 5th Feature)
  

HOW TO TRAIN:
-------------
In order to complete the training process, we atleast need 0.1 million samples of PM data. These are gathered from one slice. These samples shall be gathered from the simulation environment. Here in Simulation enviroment each time instance data are generated for all cells at interval for every 3 second verses at the acutual enviroment, each time instance data are generated for every 15 mintues. So here, We used the RAN PM Data simulation enviroment to generated data that is needed for training. The required predicted data for each time instance for the cell are synthetically generated and used in Suprevisied learning.

To run the training module by using the RAN PM data simulation
move to folder location 'ml-prediction-ms' then Run python3 train/ParseTrain.py 

The above module acquires the training data from the RAN PM data simulation. once the data are acquired via the topics. The module performs training. 

However, if the data are acquired from the Real Time RAN enviroement were each sample are generated after 15 minutes. We need to adjust the sleep time from line 146 in ParseTrain.py to generate 0.1 million samples(counts across all cells and slices).

#Wait for 2 hrs for data generation
time.sleep(3600*2) 

More Reference https://wiki.onap.org/display/DW/Smart+Intent+Guarantee+based+on+Closed-loop+-+ML+MS+Enhancements
contains the design details and requirements for training.



HOW TO LAUNCH Prediction:
-------------------------
To Run Prediction move to folder location 'ml-prediction-ms' then Run python3 src/run.py

HOW TO LAUNCH Training:
move to folder location 'ml-prediction-ms' then Run python3 train/ParseTrain.py in the command line


Training Approach that is followed to build the Machine Learning model
first the Data are acquired in offine mode, data acquisition for training will take around around 4-5 hours to have enough samples for training. After this data acuisition process the model will take around 15 minutes to run for 250 epochs with 512 batch size. The training model is desinged to auto select the best hyperparameters for the machine learning model generation.

The functionality in Training module
1. Get Slices/Cell Data: The module performs start and stop on Topics to acquire the data.
The Topics should be configured to generate Single Slice data only. In future we will support multiple slices.However, there is no limitation on the number of cells
2. Process Slices/Cells Data: The data acquired from Dmaap topic.  The data are preprocess and parsed. Here, we are synthetically generating the 5th feature I.e. ‘Predicted_maxNumberOfConns’ based on the percent increase in the ‘Failure/Total session requested’ ratios.
3. we train the model on a single slice. So we append all the cells one after the other as a part of training statergy.
- Now we have the data in the form of single cell.
- We have increased the number of training samples.


In Train functions following activities are performed.
1. The data is divided into 2 categories: a)Training set (Used for the training). b)Test Data (Used for test/validation). After this step, the data is ready to train a model.
2. Train: This will take the training set created in the last step, normalize it using MinMaxScaler and then 'series_to_supervised' method converts the timeseries into Forecast series to be fed to the LSTM.
3. After some more pre-processing, the data is fed to the Keras sequential model which contains 20 stacked LSTMs and 1 dense layer at the end.
4. The best model is chosen after each checkpoints based on the 'validation loss' and archived to be used for predictions.
