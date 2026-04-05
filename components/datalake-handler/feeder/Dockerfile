# ============LICENSE_START===================================================
#  Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
FROM eclipse-temurin:11-jre-alpine

LABEL maintainer="Guobiao Mo <guobiaomo@chinamobile.com>"

ARG user=datalake
ARG group=datalake

ARG MAX_RAM_PERCENTAGE=75
ENV MAX_RAM_PERCENTAGE=${MAX_RAM_PERCENTAGE}

EXPOSE 1680

USER root

WORKDIR /datalake
RUN addgroup $group && adduser --system --shell /bin/false --disabled-password --no-create-home --ingroup $group $user && \
    chown -R $user:$group /datalake && \
    chmod g+s /datalake && \
    mkdir -p /datalake/db_init

#add the fat jar
COPY --chown=$user:$group target/${JAR_FILE} .
COPY --chown=$user:$group src/assembly/run.sh .
COPY --chown=$user:$group src/assembly/init_db/scripts/db_init ./db_init/
COPY --chown=$user:$group src/assembly/init_db/db_scripts/init_db.sql .
COPY --chown=$user:$group src/assembly/init_db/db_scripts/init_db_data.sql .

RUN chmod 0644 *.sql && \
    chmod u+x run.sh && \
    apk add --no-cache postgresql-client curl

USER $user
ENTRYPOINT ["/bin/sh"]
CMD ["run.sh"]
