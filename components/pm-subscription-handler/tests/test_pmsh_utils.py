# ============LICENSE_START===================================================
#  Copyright (C) 2019-2021 Nordix Foundation.
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
from test.support import EnvironmentVarGuard
from unittest.mock import patch, Mock

import responses
from jsonschema import ValidationError
from requests import Session
from tenacity import RetryError

from mod import get_db_connection_url
from mod.network_function import NetworkFunction
from tests.base_setup import BaseClassSetup
from tests.base_setup import get_pmsh_config


class PmshUtilsTestCase(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    def setUp(self):
        super().setUp()
        self.mock_app = Mock()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    def test_utils_get_mr_sub(self):
        mr_policy_sub = self.app_conf.get_mr_sub('policy_pm_subscriber')
        self.assertTrue(mr_policy_sub.aaf_id, 'dcae@dcae.onap.org')

    def test_utils_get_mr_sub_fails_with_invalid_name(self):
        with self.assertRaises(KeyError):
            self.app_conf.get_mr_sub('invalid_sub')

    def test_utils_get_mr_pub(self):
        mr_policy_pub = self.app_conf.get_mr_pub('policy_pm_publisher')
        self.assertTrue(mr_policy_pub.aaf_pass, 'demo123456!')

    def test_utils_get_mr_pub_fails_with_invalid_name(self):
        with self.assertRaises(KeyError):
            self.app_conf.get_mr_pub('invalid_pub')

    def test_utils_get_cert_data(self):
        self.assertEqual(self.app_conf.cert_params, ('/opt/app/pmsh/etc/certs/cert.pem',
                                                     '/opt/app/pmsh/etc/certs/key.pem'))

    @patch.object(Session, 'post')
    def test_mr_pub_publish_to_topic_success(self, mock_session):
        mock_session.return_value.status_code = 200
        mr_policy_pub = self.app_conf.get_mr_pub('policy_pm_publisher')
        with patch('requests.Session.post') as session_post_call:
            mr_policy_pub.publish_to_topic({"dummy_val": "43c4ee19-6b8d-4279-a80f-c507850aae47"})
            session_post_call.assert_called_once()

    @responses.activate
    def test_mr_pub_publish_to_topic_fail(self):
        responses.add(responses.POST,
                      'https://message-router:3905/events/org.onap.dmaap.mr.PM_SUBSCRIPTIONS',
                      json={'error': 'Client Error'}, status=400)
        mr_policy_pub = self.app_conf.get_mr_pub('policy_pm_publisher')
        with self.assertRaises(Exception):
            mr_policy_pub.publish_to_topic({"dummy_val": "43c4ee19-6b8d-4279-a80f-c507850aae47"})

    def test_mr_pub_publish_sub_event_data_success(self):
        mr_policy_pub = self.app_conf.get_mr_pub('policy_pm_publisher')
        with patch('mod.pmsh_utils._MrPub.publish_to_topic') as pub_to_topic_call:
            mr_policy_pub.publish_subscription_event_data(
                self.app_conf.subscription,
                NetworkFunction(nf_name='pnf_1',
                                model_invariant_id='some-id',
                                model_version_id='some-id'),
                self.app_conf)
            pub_to_topic_call.assert_called_once()

    @responses.activate
    def test_mr_sub_get_from_topic_success(self):
        policy_mr_sub = self.app_conf.get_mr_sub('policy_pm_subscriber')
        responses.add(responses.GET,
                      'https://message-router:3905/events/org.onap.dmaap.mr.PM_SUBSCRIPTIONS/'
                      'dcae_pmsh_cg/1?timeout=1000',
                      json={"dummy_val": "43c4ee19-6b8d-4279-a80f-c507850aae47"}, status=200)
        mr_topic_data = policy_mr_sub.get_from_topic(1)
        self.assertIsNotNone(mr_topic_data)

    @responses.activate
    def test_mr_sub_get_from_topic_fail(self):
        policy_mr_sub = self.app_conf.get_mr_sub('policy_pm_subscriber')
        responses.add(responses.GET,
                      'https://message-router:3905/events/org.onap.dmaap.mr.PM_SUBSCRIPTIONS/'
                      'dcae_pmsh_cg/1?timeout=1000',
                      json={"dummy_val": "43c4ee19-6b8d-4279-a80f-c507850aae47"}, status=400)
        with self.assertRaises(Exception):
            policy_mr_sub.get_from_topic(1)

    def test_get_db_connection_url_success(self):
        self.env = EnvironmentVarGuard()
        self.env.set('PMSH_PG_URL', '1.2.3.4')
        self.env.set('PMSH_PG_USERNAME', 'pmsh')
        self.env.set('PMSH_PG_PASSWORD', 'pass')
        db_url = get_db_connection_url()
        self.assertEqual(db_url, 'postgresql+psycopg2://pmsh:pass@1.2.3.4:5432/pmsh')

    def test_get_db_connection_url_fail(self):
        self.env = EnvironmentVarGuard()
        self.env.set('PMSH_PG_USERNAME', 'pmsh')
        self.env.set('PMSH_PG_PASSWORD', 'pass')
        with self.assertRaises(Exception):
            get_db_connection_url()

    @patch('mod.logger.info')
    @patch('mod.pmsh_utils.get_all')
    def test_refresh_config_success(self, mock_cbs_client_get_all, mock_logger):
        mock_cbs_client_get_all.return_value = get_pmsh_config()
        self.app_conf.refresh_config()
        mock_logger.assert_called_with('AppConfig data has been refreshed')

    @patch('mod.logger.error')
    @patch('mod.pmsh_utils.get_all')
    def test_refresh_config_fail(self, mock_cbs_client_get_all, mock_logger):
        mock_cbs_client_get_all.side_effect = ValueError
        with self.assertRaises(RetryError):
            self.app_conf.refresh_config()
        mock_logger.assert_called_with('Failed to refresh PMSH AppConfig')

    @patch('mod.logger.debug')
    def test_utils_validate_config_subscription(self, mock_logger):
        self.app_conf.validate_sub_schema()
        mock_logger.assert_called_with("Subscription schema is valid.")

    @patch('mod.logger.debug')
    def test_utils_validate_config_subscription_administrativeState_locked(self, mock_logger):
        self.app_conf.subscription.administrativeState = "LOCKED"
        self.app_conf.validate_sub_schema()
        mock_logger.assert_called_with("Subscription schema is valid.")

    def test_utils_validate_config_subscription_administrativeState_invalid_value(self):
        self.app_conf.subscription.administrativeState = "FAILED"
        with self.assertRaises(ValidationError):
            self.app_conf.validate_sub_schema()

    def test_utils_validate_config_subscription_nfFilter_failed(self):
        self.app_conf.subscription.nfFilter = {}
        with self.assertRaises(ValidationError):
            self.app_conf.validate_sub_schema()

    def test_utils_validate_config_subscription_nfFilter_not_empty(self):
        self.app_conf.subscription.nfFilter = {
            "nfNames": [

            ],
            "modelInvariantIDs": [

            ],
            "modelVersionIDs": [

            ],
            "modelNames": [

            ]
        }
        with self.assertRaises(ValidationError):
            self.app_conf.validate_sub_schema()

    @patch('mod.logger.debug')
    def test_utils_validate_config_subscription_nfFilter_with_empty_property(self, mock_logger):
        self.app_conf.subscription.nfFilter = {
            "nfNames": [
                "^pnf.*",
                "^vnf.*"
            ],
            "modelInvariantIDs": [
                "7129e420-d396-4efb-af02-6b83499b12f8"
            ],
            "modelVersionIDs": [

            ],
            "modelNames": [
                "pnf102"
            ]
        }
        self.app_conf.validate_sub_schema()
        mock_logger.assert_called_with("Subscription schema is valid.")

    def test_utils_validate_config_subscription_where_measurementTypes_is_empty(self):
        self.app_conf.subscription.measurementGroups = [{
            "measurementGroup": {
                "measurementTypes": [
                ],
                "managedObjectDNsBasic": [
                    {
                        "DN": "dna"
                    },
                    {
                        "DN": "dnb"
                    }
                ]
            }
        }]
        with self.assertRaises(ValidationError):
            self.app_conf.validate_sub_schema()

    def test_utils_validate_config_subscription_where_managedObjectDNsBasic_is_empty(self):
        self.app_conf.subscription.measurementGroups = [{
            "measurementGroup": {
                "measurementTypes": [
                    {
                        "measurementType": "countera"
                    },
                    {
                        "measurementType": "counterb"
                    }
                ],
                "managedObjectDNsBasic": [

                ]
            }
        }]
        with self.assertRaises(ValidationError):
            self.app_conf.validate_sub_schema()

    def test_utils_validate_config_subscription_where_measurementGroups_is_empty(self):
        self.app_conf.subscription.measurementGroups = []
        with self.assertRaises(ValidationError):
            self.app_conf.validate_sub_schema()
