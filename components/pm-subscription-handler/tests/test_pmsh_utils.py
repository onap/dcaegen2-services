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
import json
import os
from test.support import EnvironmentVarGuard
from unittest import TestCase
from unittest.mock import patch

import responses
from requests import Session
from tenacity import RetryError, stop_after_attempt

from mod import db, get_db_connection_url, create_app
from mod.pmsh_utils import AppConfig


class PmshUtilsTestCase(TestCase):

    @patch('mod.update_logging_config')
    @patch('mod.create_app')
    @patch('mod.get_db_connection_url')
    def setUp(self, mock_get_db_url, mock_app, mock_update_config):
        mock_get_db_url.return_value = 'sqlite://'
        with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data_1.json'), 'r') as data:
            self.cbs_data = json.load(data)
        self.env = EnvironmentVarGuard()
        self.env.set('LOGGER_CONFIG', os.path.join(os.path.dirname(__file__), 'log_config.yaml'))
        self.mock_app = mock_app
        self.app = create_app()
        self.app_context = self.app.app_context()
        self.app_context.push()
        db.create_all()

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    def test_utils_get_mr_sub(self, mock_get_pmsh_config):
        mock_get_pmsh_config.return_value = self.cbs_data
        self.app_conf = AppConfig()
        mr_policy_sub = self.app_conf.get_mr_sub('policy_pm_subscriber')
        self.assertTrue(mr_policy_sub.aaf_id, 'dcae@dcae.onap.org')

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    def test_utils_get_mr_sub_fails_with_invalid_name(self, mock_get_pmsh_config):
        mock_get_pmsh_config.return_value = self.cbs_data
        self.app_conf = AppConfig()
        with self.assertRaises(KeyError):
            self.app_conf.get_mr_sub('invalid_sub')

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    def test_utils_get_mr_pub(self, mock_get_pmsh_config):
        mock_get_pmsh_config.return_value = self.cbs_data
        self.app_conf = AppConfig()
        mr_policy_pub = self.app_conf.get_mr_pub('policy_pm_publisher')
        self.assertTrue(mr_policy_pub.aaf_pass, 'demo123456!')

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    def test_utils_get_mr_pub_fails_with_invalid_name(self, mock_get_pmsh_config):
        mock_get_pmsh_config.return_value = self.cbs_data
        self.app_conf = AppConfig()
        with self.assertRaises(KeyError):
            self.app_conf.get_mr_pub('invalid_pub')

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    def test_utils_get_cert_data(self, mock_get_pmsh_config):
        mock_get_pmsh_config.return_value = self.cbs_data
        self.app_conf = AppConfig()
        self.assertTrue(self.app_conf.cert_params, ('/opt/app/pm-mapper/etc/certs/cert.pem',
                                                    '/opt/app/pm-mapper/etc/certs/key.pem'))

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    @patch.object(Session, 'post')
    def test_mr_pub_publish_to_topic_success(self, mock_session, mock_get_pmsh_config):
        mock_get_pmsh_config.return_value = self.cbs_data
        self.app_conf = AppConfig()
        mock_session.return_value.status_code = 200
        mr_policy_pub = self.app_conf.get_mr_pub('policy_pm_publisher')
        with patch('requests.Session.post') as session_post_call:
            mr_policy_pub.publish_to_topic({"dummy_val": "43c4ee19-6b8d-4279-a80f-c507850aae47"})
            session_post_call.assert_called_once()

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    @responses.activate
    def test_mr_pub_publish_to_topic_fail(self, mock_get_pmsh_config):
        mock_get_pmsh_config.return_value = self.cbs_data
        self.app_conf = AppConfig()
        responses.add(responses.POST,
                      'https://node:30226/events/org.onap.dmaap.mr.PM_SUBSCRIPTIONS',
                      json={'error': 'Client Error'}, status=400)
        mr_policy_pub = self.app_conf.get_mr_pub('policy_pm_publisher')
        with self.assertRaises(Exception):
            mr_policy_pub.publish_to_topic.retry.stop = stop_after_attempt(1)
            mr_policy_pub.publish_to_topic({"dummy_val": "43c4ee19-6b8d-4279-a80f-c507850aae47"})

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    def test_mr_pub_publish_sub_event_data_success(self, mock_get_pmsh_config):
        mock_get_pmsh_config.return_value = self.cbs_data
        self.app_conf = AppConfig()
        mr_policy_pub = self.app_conf.get_mr_pub('policy_pm_publisher')
        with patch('mod.pmsh_utils._MrPub.publish_to_topic') as pub_to_topic_call:
            mr_policy_pub.publish_subscription_event_data(self.app_conf.subscription, 'pnf201',
                                                          self.app_conf)
            pub_to_topic_call.assert_called_once()

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    @responses.activate
    def test_mr_sub_get_from_topic_success(self, mock_get_pmsh_config):
        mock_get_pmsh_config.return_value = self.cbs_data
        self.app_conf = AppConfig()
        policy_mr_sub = self.app_conf.get_mr_sub('policy_pm_subscriber')
        responses.add(responses.GET,
                      'https://node:30226/events/org.onap.dmaap.mr.PM_SUBSCRIPTIONS/'
                      'dcae_pmsh_cg/1?timeout=1000',
                      json={"dummy_val": "43c4ee19-6b8d-4279-a80f-c507850aae47"}, status=200)
        mr_topic_data = policy_mr_sub.get_from_topic(1)
        self.assertIsNotNone(mr_topic_data)

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    @responses.activate
    def test_mr_sub_get_from_topic_fail(self, mock_get_pmsh_config):
        mock_get_pmsh_config.return_value = self.cbs_data
        self.app_conf = AppConfig()
        policy_mr_sub = self.app_conf.get_mr_sub('policy_pm_subscriber')
        responses.add(responses.GET,
                      'https://node:30226/events/org.onap.dmaap.mr.PM_SUBSCRIPTIONS/'
                      'dcae_pmsh_cg/1?timeout=1000',
                      json={"dummy_val": "43c4ee19-6b8d-4279-a80f-c507850aae47"}, status=400)
        with self.assertRaises(Exception):
            policy_mr_sub.get_from_topic.retry.stop = stop_after_attempt(1)
            policy_mr_sub.get_from_topic(1)

    def test_get_db_connection_url_success(self):
        self.env = EnvironmentVarGuard()
        self.env.set('PMSH_PG_URL', '1.2.3.4')
        self.env.set('PMSH_PG_USERNAME', 'pmsh')
        self.env.set('PMSH_PG_PASSWORD', 'pass')
        db_url = get_db_connection_url()
        self.assertEqual(db_url, 'postgres+psycopg2://pmsh:pass@1.2.3.4:5432/pmsh')

    def test_get_db_connection_url_fail(self):
        self.env = EnvironmentVarGuard()
        self.env.set('PMSH_PG_USERNAME', 'pmsh')
        self.env.set('PMSH_PG_PASSWORD', 'pass')
        with self.assertRaises(Exception):
            get_db_connection_url()

    @patch('mod.logger.info')
    @patch('mod.pmsh_utils.get_all')
    def test_refresh_config_success(self, mock_cbs_client_get_all, mock_logger):
        mock_cbs_client_get_all.return_value = self.cbs_data
        self.app_conf = AppConfig()
        self.app_conf.refresh_config()
        mock_logger.assert_called_with('AppConfig data has been refreshed')

    @patch('mod.logger.debug')
    @patch('mod.pmsh_utils.get_all')
    def test_refresh_config_fail(self, mock_cbs_client_get_all, mock_logger):
        mock_cbs_client_get_all.return_value = self.cbs_data
        self.app_conf = AppConfig()
        mock_cbs_client_get_all.side_effect = Exception
        with self.assertRaises(RetryError):
            self.app_conf.refresh_config()
        mock_logger.assert_called_with('Failed to refresh AppConfig data')
