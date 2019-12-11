# ============LICENSE_START===================================================
#  Copyright (C) 2019 Nordix Foundation.
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
import unittest
from unittest import mock
from unittest.mock import patch

import responses
from requests import Session

from mod.pmsh_utils import AppConfig
from mod.subscription import Subscription


class PmshUtilsTestCase(unittest.TestCase):

    def setUp(self):
        with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data.json'), 'r') as data:
            self.cbs_data = json.load(data)
        self.app_conf = AppConfig(**self.cbs_data['config'])
        self.sub = Subscription(**self.cbs_data['policy']['subscription'])

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
        self.assertTrue(self.app_conf.get_cert_params, ('/opt/app/pm-mapper/etc/certs/cert.pem',
                                                        '/opt/app/pm-mapper/etc/certs/key.pem'))

    @mock.patch.object(Session, 'post')
    def test_mr_pub_publish_to_topic_success(self, mock_session):
        mock_session.return_value.status_code = 200
        mr_policy_pub = self.app_conf.get_mr_pub('policy_pm_publisher')
        with patch('requests.Session.post') as session_post_call:
            mr_policy_pub.publish_to_topic({"dummy_val": "43c4ee19-6b8d-4279-a80f-c507850aae47"})
            session_post_call.assert_called_once()

    @responses.activate
    def test_mr_pub_publish_to_topic_fail(self):
        responses.add(responses.POST,
                      'https://node:30226/events/org.onap.dmaap.mr.PM_SUBSCRIPTIONS',
                      json={'error': 'Client Error'}, status=400)
        mr_policy_pub = self.app_conf.get_mr_pub('policy_pm_publisher')
        with self.assertRaises(Exception):
            mr_policy_pub.publish_to_topic({"dummy_val": "43c4ee19-6b8d-4279-a80f-c507850aae47"})

    def test_mr_pub_publish_sub_event_data_success(self):
        mr_policy_pub = self.app_conf.get_mr_pub('policy_pm_publisher')
        with patch('mod.pmsh_utils._MrPub.publish_to_topic') as pub_to_topic_call:
            mr_policy_pub.publish_subscription_event_data(self.sub, 'pnf201')
            pub_to_topic_call.assert_called_once()

    @responses.activate
    def test_mr_sub_get_from_topic_success(self):
        responses.add(responses.GET,
                      'https://node:30226/events/org.onap.dmaap.mr.PM_SUBSCRIPTIONS/'
                      'dcae_pmsh_cg/1?timeout=1000',
                      json={"dummy_val": "43c4ee19-6b8d-4279-a80f-c507850aae47"}, status=200)
        mr_policy_sub = self.app_conf.get_mr_sub('policy_pm_subscriber')
        mr_topic_data = mr_policy_sub.get_from_topic(1)
        self.assertIsNotNone(mr_topic_data)

    @responses.activate
    def test_mr_sub_get_from_topic_fail(self):
        responses.add(responses.GET,
                      'https://node:30226/events/org.onap.dmaap.mr.PM_SUBSCRIPTIONS/'
                      'dcae_pmsh_cg/1?timeout=1000',
                      json={"dummy_val": "43c4ee19-6b8d-4279-a80f-c507850aae47"}, status=400)
        mr_policy_sub = self.app_conf.get_mr_sub('policy_pm_subscriber')
        mr_topic_data = mr_policy_sub.get_from_topic(1)
        self.assertIsNone(mr_topic_data)
