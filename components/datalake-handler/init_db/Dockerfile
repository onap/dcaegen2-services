# ============LICENSE_START==========================================
# ===================================================================
#  Copyright (c) 2020 QCT
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#============LICENSE_END============================================

FROM ubuntu:16.04 AS builder
MAINTAINER Kate Hsuan <kate.hsuan@qct.io>

LABEL org.label-schema.docker.build="docker build -t dl-db-init . --no-cache" \
      org.label-schema.docker.run="docker run dl-init"

WORKDIR /app
WORKDIR /app/db_init
ADD scripts/db_init/ .
RUN chmod 0755 ./*
WORKDIR /app
COPY scripts/run.sh .
COPY db_scripts/init_db.sql .
COPY db_scripts/init_db_data.sql .

RUN chmod 0755 /app/run.sh

ARG mysqlpasswd="0000"
ENV MYSQL_ROOT_PASSWORD=$mysqlpasswd

ARG mysqlhost="mariadb-galera"
ENV MYSQL_HOST=$mysqlhost

ARG mysqlport="3306"
ENV MYSQL_PORT=${mysqlport}

RUN apt update && \
    apt install -y mariadb-client-core-10.0 && \
    apt install -y curl

CMD ["sh", "run.sh"]
