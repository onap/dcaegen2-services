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
#!/bin/sh

echo "start init db ..."

/bin/run-parts /home/datalake/db_init

echo "finish init db"

cmd=`find . -regex  '\./des-[0-9]+\.[0-9]+\.[0-9]+[-SNAPSHOT]+\.jar'`
cmd1=`find . -regex '\./des-[0-9]+\.[0-9]+\.[0-9]+\.jar'`
cmd2=`find . -regex '\./des-[0-9]+\.[0-9]+\.[0-9]+[-execute]+\.jar'`
cmd3=`find . -regex  '\./des-[0-9]+\.[0-9]+\.[0-9]+[-SNAPSHOT]+[-execute]+\.jar'`
-execute
if [ -n "$cmd" ]; then
    java -jar $cmd
elif [ -n "$cmd1" ]; then
    java -jar $cmd1
elif [ -n "$cmd2" ]; then
    java -jar $cmd2
elif [ -n "$cmd3" ]; then
    java -jar $cmd3
else
    echo "STRING is empty"
    sleep 10000
fi
