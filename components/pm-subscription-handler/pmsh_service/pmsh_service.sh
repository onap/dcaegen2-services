#!/bin/bash
# ============LICENSE_START=======================================================
#  Copyright (C) 2019 Nordix Foundation.
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


# get to where we are supposed to be for startup
cd /opt/app/pmsh/bin

# include path to 3.7+ version of python that has required dependencies included
export PATH=/usr/local/lib/python3.7/bin:$PATH:/opt/app/pmsh/bin

# expand search for python modules
export PYTHONPATH=/usr/local/lib/python3.7/site-packages:./mod:./:${PYTHONPATH}:/opt/app/pmsh/bin

# set location of SSL certificates
export REQUESTS_CA_BUNDLE=/etc/ssl/certs/ca-certificates.crt

python pmsh_service.py