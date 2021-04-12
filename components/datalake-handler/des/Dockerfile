# ============LICENSE_START===================================================
#  Copyright (C) 2020 China Mobile.
#  Copyright (C) 2021 Samsung Electronics.
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
FROM nexus3.onap.org:10001/onap/integration-java11:8.0.0

LABEL maintainer="Kai Lu <lukai@chinamobile.com>"

ARG user=datalake
ARG group=datalake

EXPOSE 1681

WORKDIR /datalake

USER root
RUN addgroup $group && adduser --system --disabled-password --no-create-home --ingroup $group $user && \
    mkdir -p /datalake/ && \
    mkdir -p /datalake/db_init && \
    chown -R $user:$group /datalake

#add the fat jar
COPY --chown=$user:$group target/${JAR_FILE} .
COPY --chown=$user:$group src/assembly/run.sh .
COPY --chown=$user:$group src/assembly/init_db/db_scripts/init_db.sql .
COPY --chown=$user:$group src/assembly/init_db/db_scripts/init_db_data.sql .

ADD --chown=$user:$group src/assembly/init_db/scripts/db_init ./db_init/

RUN chmod -R 0755 ./* && \
    apk add --no-cache mariadb-client curl

USER $user
CMD ["sh", "run.sh"]