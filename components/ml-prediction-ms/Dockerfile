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



FROM nexus3.onap.org:10001/onap/integration-python:8.0.0 as build

USER root
RUN set -eux; \
    apk add \
    build-base \
    python3-dev

FROM python
RUN pip install --no-cache-dir pandas


RUN pip3 install --user --upgrade tensorflow

RUN pip3 install Flask --trusted-host pypi.org --trusted-host files.pythonhosted.org

RUN pip3 install requests --trusted-host pypi.org --trusted-host files.pythonhosted.org

RUN pip3 install -U scikit-learn scipy matplotlib
RUN pip3 install openpyxl
RUN pip3 install pytest-cov
RUN pip3 install xlrd
RUN pip3 install mock
RUN pip3 install requests
RUN pip3 install responses
RUN pip3 install configparser
RUN pip3 install requests-mock

ADD src src
ADD model model
ADD tests tests
ADD ml-prediction-ms.config ml-prediction-ms.config

EXPOSE 5000

RUN chmod 755 /src/run.py


ARG user=onap
ARG group=onap

# Tell docker that all future commands should be run as the onap user
USER $user

CMD ["/src/run.py"]
