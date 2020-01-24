# ============LICENSE_START===================================================
#  Copyright (C) 2020 Nordix Foundation.
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
import mod.pmsh_logging as logger

basedir = os.path.abspath(os.path.dirname(__file__))


def get_db_connection_url():
    pg_host = os.getenv('PMSH_PG_URL')
    pg_user = os.getenv('PMSH_PG_USERNAME')
    pg_user_pass = os.getenv('PMSH_PG_PASSWORD')
    pmsh_db_name = os.getenv('PMSH_DB_NAME', 'pmsh')
    pmsh_db_port = os.getenv('PMSH_PG_PORT', '5432')
    db_url = f'postgres+psycopg2://{pg_user}:{pg_user_pass}@{pg_host}:' \
             f'{pmsh_db_port}/{pmsh_db_name}'
    if 'None' in db_url:
        return 'sqlite:///' + os.path.join(basedir, 'pmsh_data.sqlite')
    return f'postgres+psycopg2://{pg_user}:{pg_user_pass}@{pg_host}:' \
           f'{pmsh_db_port}/{pmsh_db_name}'


class Config:
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    SQLALCHEMY_RECORD_QUERIES = True

    @staticmethod
    def init_app(app):
        pass


class TestingConfig(Config):
    TESTING = True
    logger.create_loggers('../test_logs')
    SQLALCHEMY_DATABASE_URI = os.environ.get('TEST_DB_URL', 'sqlite://')


class ProductionConfig(Config):
    SQLALCHEMY_DATABASE_URI = get_db_connection_url()

    @classmethod
    def init_app(cls, app):
        Config.init_app(app)


run_config = {
    'testing': TestingConfig,
    'production': ProductionConfig,

    'default_config': ProductionConfig
}
