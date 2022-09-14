# ============LICENSE_START=======================================================
#  Copyright (c) 2019 QCT. All rights reserved.
#  Copyright (c) 2022 AT&T. All rights reserved.
# ================================================================================
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
# ============LICENSE_END=========================================================

FROM node:alpine AS builder
MAINTAINER Ekko Chang <ekko.chang@qct.io>

LABEL org.label-schema.docker.build="docker build -t dl-admin . --no-cache" \
      org.label-schema.docker.run="docker run -p 80:80 --add-host=dl_feeder:your_feeder_ipaddress dl-admin"

ENV NODE_OPTIONS "--openssl-legacy-provider"

WORKDIR /app
COPY ./src .
COPY ./nginx .
RUN npm install && \
    npm run build


FROM nginx:1.17.9

RUN groupadd -r datalake && useradd -r -g datalake datalake

COPY --from=builder /app/dist/* /usr/share/nginx/html/
COPY --from=builder /app/dl-admin-nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/run.sh /tmp

RUN chown -R datalake:datalake /etc/nginx
RUN chown -R datalake:datalake /var/cache/nginx
RUN chown -R datalake:datalake /tmp/run.sh
RUN chmod 0744 /tmp/run.sh


USER datalake

CMD ["sh", "-c", "/tmp/run.sh"]

