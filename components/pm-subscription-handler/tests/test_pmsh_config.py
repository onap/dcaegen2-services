# ============LICENSE_START===================================================
#  Copyright (C) 2021 Nordix Foundation.
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
from unittest.mock import Mock, patch

import responses
from requests import Session

from mod.pmsh_config import MRTopic, AppConfig
from tests.base_setup import BaseClassSetup


class PmshConfigTestCase(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    def setUp(self):
        super().setUpAppConf()
        self.mock_app = Mock()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    def test_config_get_aaf_creds(self):
        self.assertEqual(self.pmsh_app_conf.enable_tls, 'true')
        self.assertEqual(self.pmsh_app_conf.aaf_id, 'dcae@dcae.onap.org')
        self.assertEqual(self.pmsh_app_conf.aaf_pass, 'demo123456!')

    def test_config_get_cert_data(self):
        self.assertEqual(self.pmsh_app_conf.key_path, '/opt/app/pmsh/etc/certs/key.pem')
        self.assertEqual(self.pmsh_app_conf.cert_path, '/opt/app/pmsh/etc/certs/cert.pem')
        self.assertEqual(self.pmsh_app_conf.ca_cert_path, '/opt/app/pmsh/etc/certs/cacert.pem')

    def test_singleton_instance_is_accessible_using_class_method(self):
        my_singleton_instance = AppConfig.get_instance()
        self.assertIsNotNone(my_singleton_instance)
        self.assertIsInstance(my_singleton_instance, AppConfig)

    @patch.object(Session, 'post')
    def test_mr_pub_publish_to_topic_success(self, mock_session):
        mock_session.return_value.status_code = 200
        with patch('requests.Session.post') as session_post_call:
            self.pmsh_app_conf.publish_to_topic(MRTopic.POLICY_PM_PUBLISHER.value,
                                                {"key": "43c4ee19-6b8d-4279-a80f-c507850aae47"})
            session_post_call.assert_called_once()

    @responses.activate
    def test_mr_pub_publish_to_topic_fail(self):
        responses.add(responses.POST,
                      'https://message-router:3905/events/org.onap.dmaap.mr.PM_SUBSCRIPTIONS',
                      json={"error": "Client Error"}, status=400)
        with self.assertRaises(Exception):
            self.pmsh_app_conf.publish_to_topic(MRTopic.POLICY_PM_PUBLISHER.value,
                                                {"key": "43c4ee19-6b8d-4279-a80f-c507850aae47"})

    @responses.activate
    def test_mr_sub_get_from_topic_success(self):
        responses.add(responses.GET,
                      'https://message-router:3905/events/org.onap.dmaap.mr.PM_SUBSCRIPTIONS/'
                      'dcae_pmsh_cg/1?timeout=1000',
                      json={"key": "43c4ee19-6b8d-4279-a80f-c507850aae47"}, status=200)
        mr_topic_data = self.pmsh_app_conf.get_from_topic(MRTopic.POLICY_PM_SUBSCRIBER.value, 1)
        self.assertIsNotNone(mr_topic_data)

    @responses.activate
    def test_mr_sub_get_from_topic_fail(self):
        responses.add(responses.GET,
                      'https://message-router:3905/events/org.onap.dmaap.mr.PM_SUBSCRIPTIONS/'
                      'dcae_pmsh_cg/1?timeout=1000',
                      json={"key": "43c4ee19-6b8d-4279-a80f-c507850aae47"}, status=400)
        with self.assertRaises(Exception):
            self.pmsh_app_conf.get_from_topic(MRTopic.POLICY_PM_SUBSCRIBER.value, 1)
