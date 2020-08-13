# ============LICENSE_START===================================================
#  Copyright (C) 2020 Nordix Foundation.
#  Copyright 2020 Deutsche Telekom. All rights reserved.
# ============================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=====================================================

FROM python:3.8.2-alpine3.11
MAINTAINER lego@est.tech

ARG user=onap
ARG group=onap

WORKDIR /app

# set PATH & PYTHONPATH vars
ENV PATH=/usr/local/lib/python3.8/bin:$PATH:./bin \
    PYTHONPATH=/usr/local/lib/python3.8/site-packages:./mod:./:$PYTHONPATH:./bin \
    REQUESTS_CA_BUNDLE=/etc/ssl/certs/ca-certificates.crt \
    LOGS_PATH="/var/log/ONAP/dcaegen2/services/pmsh" \
    LOGGER_CONFIG=/opt/app/pmsh/log_config.yaml

    # add non root user & group
RUN addgroup --system $user && adduser --ingroup $user --system $user && \
    # create and chown the LOGS_PATH
    apk add build-base libffi-dev postgresql-dev && \
    mkdir -p $LOGS_PATH && \
    chown -R $user:$group $LOGS_PATH

COPY setup.py ./
COPY requirements.txt ./
COPY ./pmsh_service ./bin/
COPY log_config.yaml /opt/app/pmsh/

    # run the pip install
RUN pip install --upgrade pip && \
    pip install -r requirements.txt && \
    pip install -e . && \
    # change own & perms on entrypoint
    chown -R $user:$group . && \
    chmod 500 ./bin/*.py

# run the app
ENTRYPOINT ["python", "./bin/pmsh_service_main.py"]