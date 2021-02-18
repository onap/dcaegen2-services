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
FROM nexus3.onap.org:10001/onap/integration-java11:latest

MAINTAINER Kai Lu <lukai@chinamobile.com>

EXPOSE 1681

USER root
RUN addgroup --system datalake && adduser --system --ingroup datalake datalake && \
    mkdir /home/datalake/ && \
    mkdir /home/datalake/db_init

WORKDIR /home/datalake

#add the fat jar
COPY target/${JAR_FILE} .
COPY src/assembly/run.sh .
COPY src/assembly/init_db/db_scripts/init_db.sql .
COPY src/assembly/init_db/db_scripts/init_db_data.sql .

ADD src/assembly/init_db/scripts/db_init ./db_init/

RUN chmod -R 0755 ./* && \
    chown -R datalake:datalake /home/datalake 

RUN apk install --no-cache mariadb curl

USER datalake
CMD ["sh", "run.sh"]

