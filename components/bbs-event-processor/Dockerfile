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

ARG PROJECT_BUILD_DIR_NAME
ARG FINAL_JAR
ARG DEPENDENCIES_DIR
ARG DOCKER_ARTIFACT_DIR
ARG user=bbs-ep
ARG group=bbs-ep


#Add a new user and group to allow container to be run as non-root
USER root
RUN addgroup $group && adduser --system --disabled-password --no-create-home --ingroup $group $user

#Copy dependencies and executable jar
WORKDIR ${DOCKER_ARTIFACT_DIR}
COPY --chown=${user}:${group} ${PROJECT_BUILD_DIR_NAME}/${FINAL_JAR} .
COPY --chown=${user}:${group} KeyStore.jks .
COPY --chown=${user}:${group} KeyStorePass.txt .

#Overcome Docker limitation to put ARG inside ENTRYPOINT
RUN ln -s ${FINAL_JAR} bbs-ep.jar
COPY --chown=${user}:${group} ${PROJECT_BUILD_DIR_NAME}/${DEPENDENCIES_DIR} ./${DEPENDENCIES_DIR}

EXPOSE 8100

USER ${user}
ENTRYPOINT ["java", "-jar", "bbs-ep.jar"]
