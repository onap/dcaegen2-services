# ============LICENSE_START===================================================
#  Copyright (C) 2020 Nordix Foundation.
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

FROM python:3.7-slim-buster
MAINTAINER lego@est.tech

ENV PMSHUSER=pmsh \
    APPDIR="/opt/app/pmsh" \
    # set PATH & PYTHONPATH vars
    PATH=/usr/local/lib/python3.7/bin:$PATH:$APPDIR/bin \
    PYTHONPATH=/usr/local/lib/python3.7/site-packages:./mod:./:$PYTHONPATH:$APPDIR/bin \
    REQUESTS_CA_BUNDLE=/etc/ssl/certs/ca-certificates.crt \
    LOGS_PATH="/var/log/ONAP/dcaegen2/services/pmsh" \
    LOGGER_CONFIG=/opt/app/pmsh/log_config.yaml

WORKDIR $APPDIR

    # add non root user & group
RUN addgroup --system $PMSHUSER && adduser --ingroup $PMSHUSER --system $PMSHUSER && \
    # create and chown the LOGS_PATH
    apt-get update && \
    apt-get install -y --no-install-recommends vim-tiny && \
    apt-get clean && rm -rf /var/lib/apt/lists/* && \
    mkdir -p $LOGS_PATH && \
    chown -R $PMSHUSER:$PMSHUSER $LOGS_PATH

COPY setup.py ./
COPY requirements.txt ./
COPY ./pmsh_service ./bin/
COPY log_config.yaml /opt/app/pmsh/

    # run the pip install
RUN pip install --upgrade pip && \
    pip install -r requirements.txt && \
    pip install -e . && \
    # change own & perms on entrypoint
    chown -R $PMSHUSER:$PMSHUSER $APPDIR && \
    chmod 500 $APPDIR/bin/*.py

# set to non root user
USER $PMSHUSER

# run the app
ENTRYPOINT ["python", "./bin/pmsh_service_main.py"]