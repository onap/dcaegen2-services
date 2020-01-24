# ============LICENSE_START===================================================
#  Copyright (C) 2019-2020 Nordix Foundation.
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
from os import path

from connexion import App
from flask_sqlalchemy import SQLAlchemy

from mod.env_config import run_config

db = SQLAlchemy()


def create_app(config_name):
    basedir = path.abspath(path.dirname(__file__))
    connex_app = App(__name__, specification_dir=basedir)
    app = connex_app.app
    app.config.from_object(run_config[config_name])
    run_config[config_name].init_app(app)
    db.init_app(app)
    return app
