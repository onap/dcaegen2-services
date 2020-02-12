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
import os
from urllib.parse import quote

from connexion import App
from flask_sqlalchemy import SQLAlchemy

import mod.pmsh_logging as logger

db = SQLAlchemy()
basedir = os.path.abspath(os.path.dirname(__file__))


def create_app():
    logger.create_loggers(os.getenv('LOGS_PATH'))
    connex_app = App(__name__, specification_dir=basedir)
    app = connex_app.app
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
    app.config['SQLALCHEMY_RECORD_QUERIES'] = True
    if os.getenv('TESTING') != 'True':
        app.config['SQLALCHEMY_DATABASE_URI'] = get_db_connection_url()
    else:
        app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite://'
    db.init_app(app)
    return app


def get_db_connection_url():
    pg_host = os.getenv('PMSH_PG_URL')
    pg_user = os.getenv('PMSH_PG_USERNAME')
    pg_user_pass = os.getenv('PMSH_PG_PASSWORD')
    pmsh_db_name = os.getenv('PMSH_DB_NAME', 'pmsh')
    pmsh_db_port = os.getenv('PMSH_PG_PORT', '5432')
    db_url = f'postgres+psycopg2://{quote(str(pg_user), safe="")}:' \
        f'{quote(str(pg_user_pass), safe="")}@{pg_host}:{pmsh_db_port}/{pmsh_db_name}'
    if 'None' in db_url:
        raise Exception(f'Invalid DB connection URL: {db_url} .. exiting app!')
    return db_url
