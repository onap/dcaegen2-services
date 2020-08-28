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

FROM python:3.8.2-alpine3.11 as build
MAINTAINER lego@est.tech

RUN set -eux; \
    apk add \
    build-base \
    python3-dev \
    postgresql-dev

COPY setup.py ./
COPY requirements.txt ./
RUN pip install --prefix /opt -r requirements.txt --no-cache-dir

# Second stage
FROM python:3.8.2-alpine3.11

COPY --from=build /opt /opt

ARG user=onap
ARG group=onap

# set PATH & PYTHONPATH vars
ENV APPDIR="/opt/app/pmsh" \
    LD_LIBRARY_PATH=/opt/lib:/opt/lib64 \
    PYTHONPATH=${APPDIR}/mod:/opt/lib/python3.8/site-packages \
    PATH=$PATH:${APPDIR}/bin \
    LOGS_PATH="/var/log/ONAP/dcaegen2/services/pmsh" \
    LOGGER_CONFIG="/opt/app/pmsh/log_config.yaml"

WORKDIR $APPDIR
RUN mkdir -p $APPDIR

RUN set -eux; \
    apk add --no-cache postgresql-libs nano

COPY ./pmsh_service ./bin
COPY log_config.yaml ./

# Create a group and user
RUN addgroup -S $group && adduser -S -D -h /home/$user $user $group && \
    mkdir -p ${LOGS_PATH} && \
    chown -R $user:$group ${LOGS_PATH} && \
    chown -R $user:$group ${APPDIR}

# Tell docker that all future commands should be run as the onap user
USER $user

ENTRYPOINT ["python", "/opt/app/pmsh/bin/pmsh_service_main.py"]