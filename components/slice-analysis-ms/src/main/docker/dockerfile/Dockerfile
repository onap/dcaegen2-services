# ============LICENSE_START===================================================
#  Copyright (C) 2021 Wipro Limited
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
ARG JAR_FILE
USER root
COPY /bin/${JAR_FILE} /bin/application.jar
COPY /bin/entry.sh /bin/entry.sh
RUN adduser sliceanalysis --disabled-password sliceanalysis && \
    chmod 644 /bin/application.jar && \
    chmod 755 /bin/entry.sh
USER sliceanalysis
EXPOSE 8080
ENTRYPOINT ["/bin/entry.sh"]
