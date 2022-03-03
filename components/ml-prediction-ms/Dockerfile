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



FROM nexus3.onap.org:10001/onap/integration-python:10.1.0 as build

USER root
RUN set -eux; \
    apk add \
    build-base \
    python3-dev

ARG user=onap
ARG group=onap
USER $user

COPY requirements.txt /
RUN pip3 install -r ./requirements.txt

ADD src src
ADD model model
ADD ml-prediction-ms.config ml-prediction-ms.config
ADD tests tests

EXPOSE 5000

RUN chmod 755 /src/run.py

CMD ["/src/run.py"]
