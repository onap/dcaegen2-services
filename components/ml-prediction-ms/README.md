#############################################################################
#  ============LICENSE_START=======================================================
#  ml-prediction-ms
#  ================================================================================
#   Copyright (C) 2022 Wipro Limited
#   ==============================================================================
#     Licensed under the Apache License, Version 2.0 (the "License");
#     you may not use this file except in compliance with the License.
#     You may obtain a copy of the License at
#
#          http://www.apache.org/licenses/LICENSE-2.0
#
#     Unless required by applicable law or agreed to in writing, software
#     distributed under the License is distributed on an "AS IS" BASIS,
#     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#     See the License for the specific language governing permissions and
#     limitations under the License.
#  ============LICENSE_END=========================================================
#
###############################################################################




# Changelog
All changes are logged in Changelog.md

# Overview
The ml-prediction-ms consumes the Slices and cells resource usage details as well as the success and failure connection.
The solution predicts will predict the demand of each cells across the respective slice. 
The Solution uses ML to predict recommend allocation/deallocation of resources based on the requirements. The solution validates the recommendation across all the other cells and based on rule condition and priorities grant the recommendation based on the rule validations. 


# Usage to predict model
# usage to training

# Assumptions
1. The development is in intermediate stages, Its a partial feature release with the full enviroment details 
2. Shortly will have the full version


# Development
## Version changes
Development changes require a version python3-pandas, tensorflow, Flask & requests. 



