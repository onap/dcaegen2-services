# ============LICENSE_START===================================================
#  Copyright (C) 2020 China Mobile.
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
FROM openjdk:11-jre-slim

MAINTAINER Kai Lu <lukai@chinamobile.com>

EXPOSE 1681

RUN groupadd -r datalake && useradd -r -g datalake datalake
RUN mkdir /home/datalake/

USER datalake
 
WORKDIR /home/datalake

#add the fat jar
COPY target/${JAR_FILE} /home/datalake/
COPY src/assembly/run.sh /home/datalake/

WORKDIR /home/datalake/db_init
ADD src/assembly/init_db/scripts/db_init .
USER root
RUN chmod 0755 ./*
WORKDIR /home/datalake
COPY src/assembly/init_db/db_scripts/init_db.sql .
COPY src/assembly/init_db/db_scripts/init_db_data.sql .

RUN apt update && \
    apt install -y mariadb-client && \
    apt install -y curl

USER datalake

CMD ["sh", "run.sh"]


