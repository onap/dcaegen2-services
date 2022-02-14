# ============LICENSE_START=======================================================
#  Copyright (C) 2019-2022 Nordix Foundation.
#  Copyright (C) 2021 AT&T Property. All rights reserved.
#  Copyright 2020 Deutsche Telekom. All rights reserved.
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

from setuptools import setup, find_packages

setup(
    name="pm_subscription_handler",
    version="3.0.0",
    packages=find_packages(),
    author="lego@est.tech",
    author_email="lego@est.tech",
    license='Apache 2',
    description="Service to handle PM subscriptions",
    url="https://gerrit.onap.org/r/#/admin/projects/dcaegen2/services",
    python_requires='>=3',
    install_requires=[
        "requests==2.22.0",
        "tenacity==6.0.0",
        "connexion==2.5.0",
        "flask_sqlalchemy==2.4.1",
        "Flask==1.1.1",
        "tornado==6.1",
        "swagger-ui-bundle==0.0.6",
        "psycopg2-binary==2.8.6",
        "onap_dcae_cbs_docker_client==2.2.1",
        "onappylog==1.0.9",
        "ruamel.yaml==0.16.10",
        "jsonschema==3.2.0"]
)
