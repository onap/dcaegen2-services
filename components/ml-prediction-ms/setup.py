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

from setuptools import setup, find_packages

setup(
    name="ml-prediction-ms",
    version="1.0.0",
    author="sendil.kumar@wipro.com",
    author_email="sendil.kumar@wipro.com",
    license='Apache 2',
    description="Slice Intelligence Machine Learning Prediction",
    url="https://gerrit.onap.org/r/gitweb?p=dcaegen2/services.git;a=tree;f=components;hb=HEAD",
    packages=find_packages()
)
