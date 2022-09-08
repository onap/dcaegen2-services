#!/bin/bash
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


echo "start init db ..."

/bin/run-parts /app/db_init

echo "finish init db"


curl -X PUT -H "contain-type:application/json" http://consul:8500/v1/kv/k8s-datalake -d "{\"mysql_host\": \"$MYSQL_HOST\", \"mysql_password\": \"$MYSQL_ROOT_PASSWORD\", \"mysql_port\": \"$MYSQL_PORT\", \"presto_host\": \"$PRESTO_HOST\"}"

