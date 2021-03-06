# ============LICENSE_START===================================================
#  Copyright (C) 2020 China Mobile.
#  Copyright (C) 2021 Wipro Limited.
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

#!/bin/sh

echo "start init db ..."

export PGPASSWORD=$PG_PASSWORD

sh db_init/11_create-database
sh db_init/20_db-initdb

echo "finish init db"

cmd=`find . -name "des*-execute.jar"`
if [ -n "$cmd" ]; then
    java -jar "$cmd"
else
    echo "STRING is empty"
    sleep 10000
fi
