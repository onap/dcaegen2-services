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

FROM nexus3.onap.org:10001/onap/integration-java11:10.0.0
ADD /bin/${JAR_FILE} /bin/
ADD /bin/entry.sh /bin/
USER root
RUN adduser sliceanalysis --disabled-password sliceanalysis
RUN mv /bin/*.jar /bin/application.jar
RUN chmod -R 777 /bin
RUN chmod +x /bin/entry.sh
USER sliceanalysis
EXPOSE 8080
ENTRYPOINT /bin/entry.sh
