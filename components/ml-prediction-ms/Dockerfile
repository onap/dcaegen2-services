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

#working but create 3.5GB space
#FROM python:latest
#RUN apt-get update
#RUN apt-get -y install python3-pandas

FROM python
RUN pip install --no-cache-dir pandas


RUN pip3 install --user --upgrade tensorflow

RUN pip3 install Flask --trusted-host pypi.org --trusted-host files.pythonhosted.org

RUN pip3 install requests --trusted-host pypi.org --trusted-host files.pythonhosted.org

ADD src src

EXPOSE 5000

CMD ["python3", "/src/run.py"]
