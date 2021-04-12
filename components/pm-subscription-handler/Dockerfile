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

FROM nexus3.onap.org:10001/onap/integration-python:8.0.0 as build
LABEL maintainer="lego@est.tech"

USER root
RUN set -eux; \
    apk add \
    build-base \
    python3-dev \
    postgresql-dev

COPY setup.py ./
COPY requirements.txt ./
RUN pip install --prefix /opt -r requirements.txt --no-cache-dir

# Second stage
FROM nexus3.onap.org:10001/onap/integration-python:8.0.0

COPY --from=build /opt /opt

USER root
ARG user=onap
ARG group=onap

# set PATH & PYTHONPATH vars
ENV APPDIR="/opt/app/pmsh" \
    LD_LIBRARY_PATH=/opt/lib \
    PYTHONPATH=${APPDIR}/mod:/opt/lib/python3.9/site-packages \
    PATH=$PATH:${APPDIR}/bin \
    LOGS_PATH="/var/log/ONAP/dcaegen2/services/pmsh" \
    LOGGER_CONFIG="/opt/app/pmsh/log_config.yaml"

WORKDIR $APPDIR
RUN mkdir -p $APPDIR

RUN set -eux; \
    apk add --no-cache postgresql-libs

COPY ./pmsh_service ./bin
COPY log_config.yaml ./

# onap user & group already avaliable, create home dir
RUN mkdir -p /home/$user && \
    chown -R $user:$group /home/$user && \
    mkdir -p ${LOGS_PATH} && \
    chown -R $user:$group ${LOGS_PATH} && \
    chown -R $user:$group ${APPDIR}

# Tell docker that all future commands should be run as the onap user
USER $user

ENTRYPOINT ["python", "/opt/app/pmsh/bin/pmsh_service_main.py"]
