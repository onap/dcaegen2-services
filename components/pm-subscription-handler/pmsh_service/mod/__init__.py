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
import logging as logging
import os
import pathlib
from urllib.parse import quote

from connexion import App
from flask_sqlalchemy import SQLAlchemy
from onaplogging import monkey
from onaplogging.mdcContext import MDC
from ruamel.yaml import YAML

db = SQLAlchemy()
basedir = os.path.abspath(os.path.dirname(__file__))
_connexion_app = None
logger = logging.getLogger('onap_logger')


def _get_app():
    global _connexion_app
    if not _connexion_app:
        _connexion_app = App(__name__, specification_dir=basedir)
    return _connexion_app


def launch_api_server(app_config):
    connex_app = _get_app()
    connex_app.add_api('api/pmsh_swagger.yml')
    connex_app.run(port=os.environ.get('PMSH_API_PORT', '8443'),
                   ssl_context=(app_config.cert_path, app_config.key_path))


def create_app():
    create_logger()
    connex_app = _get_app()
    app = connex_app.app
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
    app.config['SQLALCHEMY_RECORD_QUERIES'] = True
    app.config['SQLALCHEMY_DATABASE_URI'] = get_db_connection_url()
    db.init_app(app)
    return app


def create_logger():
    config_file_path = os.getenv('LOGGER_CONFIG')
    update_config(config_file_path)
    monkey.patch_loggingYaml()
    logging.config.yamlConfig(filepath=config_file_path,
                              watchDog=os.getenv('DYNAMIC_LOGGER_CONFIG', True))
    old_record = logging.getLogRecordFactory()

    def augment_record(*args, **kwargs):
        new_record = old_record(*args, **kwargs)
        new_record.mdc = MDC.result()
        return new_record

    logging.setLogRecordFactory(augment_record)


def update_config(config_file_path):
    config_yaml = YAML()
    config_file = pathlib.Path(config_file_path)
    data = config_yaml.load(config_file)
    data['handlers']['onap_log_handler']['filename'] = \
        f'{os.getenv("LOGS_PATH")}/application.log'
    config_yaml.dump(data, config_file)


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
