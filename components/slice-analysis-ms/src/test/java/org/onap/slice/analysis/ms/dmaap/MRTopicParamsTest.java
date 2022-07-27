/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Huawei Canada Limited.
 *   Modifications Copyright (C) 2022 CTC, Inc.
 *   ==============================================================================
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     ============LICENSE_END=========================================================
 *
 *******************************************************************************/

package org.onap.slice.analysis.ms.dmaap;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MRTopicParamsTest {
    private static final String TEST_TOPIC = "test-topic";
    private static final String TEST_HOST = "test-host";
    private static final String MY_CLIENT = "my-client";
    private static final String MY_CG = "my-cg";
    private static final String MY_CI = "my-ci";
    private static final String MY_API_SEC = "my-api-sec";
    private static final String MY_API_KEY = "my-api-key";
    private static final int MY_FETCH_LIMIT = 100;
    private static final int MY_FETCH_TIMEOUT = 1000;
    private static final String MY_PASS = "my-pass";
    private static final String MY_USERNAME = "my-username";
    private static final int MY_PORT = 5555;

    @Test
    public void builderTest() {
        MRTopicParams params = MRTopicParams.builder()
                .topic(TEST_TOPIC)
                .hostname(TEST_HOST)
                .clientName(MY_CLIENT)
                .consumerGroup(MY_CG)
                .consumerInstance(MY_CI)
                .apiSecret(MY_API_SEC)
                .apiKey(MY_API_KEY)
                .fetchLimit(MY_FETCH_LIMIT)
                .fetchTimeout(MY_FETCH_TIMEOUT)
                .password(MY_PASS)
                .userName(MY_USERNAME)
                .port(MY_PORT)
                .build();

        assertEquals(TEST_TOPIC, params.getTopic());
        assertEquals(TEST_HOST, params.getHostname());
        assertEquals(MY_CLIENT, params.getClientName());
        assertEquals(MY_CG, params.getConsumerGroup());
        assertEquals(MY_CI, params.getConsumerInstance());
        assertEquals(MY_API_SEC, params.getApiSecret());
        assertEquals(MY_API_KEY, params.getApiKey());
        assertEquals(MY_FETCH_LIMIT, params.getFetchLimit());
        assertEquals(MY_FETCH_TIMEOUT, params.getFetchTimeout());
        assertEquals(MY_PASS, params.getPassword());
        assertEquals(MY_USERNAME, params.getUserName());
        assertEquals(MY_PORT, params.getPort());
    }

    @Test
    public void testGetterSetterMRTopicParams() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MRTopicParams.class);
        validateMd(pojoclass);
    }

    public void validateMd(PojoClass pojoclass) {
        Validator validator = ValidatorBuilder
                .create()
                .with(new SetterMustExistRule())
                .with(new GetterMustExistRule())
                .with(new SetterTester())
                .with(new GetterTester())
                .build();
        validator.validate(pojoclass);
    }

    @Test
    public void initProperty() {
        List<String> strings = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        MRTopicParams params = MRTopicParams.builder()
                .topic(TEST_TOPIC)
                .hostname(TEST_HOST)
                .clientName(MY_CLIENT)
                .consumerGroup(MY_CG)
                .consumerInstance(MY_CI)
                .apiSecret(MY_API_SEC)
                .apiKey(MY_API_KEY)
                .fetchLimit(MY_FETCH_LIMIT)
                .fetchTimeout(MY_FETCH_TIMEOUT)
                .password(MY_PASS)
                .userName(MY_USERNAME)
                .port(MY_PORT)
                .servers(strings)
                .additionalProps(map)
                .effectiveTopic("")
                .useHttps(true)
                .allowSelfSignedCerts(true)
                .managed(true)
                .environment("")
                .aftEnvironment("")
                .partner("")
                .latitude("")
                .longitude("")
                .partitionId("")
                .basePath("")
                .serializationProvider("")
                .build();
        assertEquals(TEST_TOPIC, params.getTopic());
        assertEquals(TEST_HOST, params.getHostname());
        assertEquals(MY_CLIENT, params.getClientName());
        assertEquals(MY_CG, params.getConsumerGroup());
        assertEquals(MY_CI, params.getConsumerInstance());
        assertEquals(MY_API_SEC, params.getApiSecret());
        assertEquals(MY_API_KEY, params.getApiKey());
        assertEquals(MY_FETCH_LIMIT, params.getFetchLimit());
        assertEquals(MY_FETCH_TIMEOUT, params.getFetchTimeout());
        assertEquals(MY_PASS, params.getPassword());
        assertEquals(MY_USERNAME, params.getUserName());
        assertEquals(MY_PORT, params.getPort());
        assertEquals(strings, params.getServers());
        assertEquals(map, params.getAdditionalProps());
        assertEquals("", params.getEffectiveTopic());
        assertTrue(params.isUseHttps());
        assertTrue(params.isAllowSelfSignedCerts());
        assertTrue(params.isManaged());
        assertEquals("", params.getEnvironment());
        assertEquals("", params.getAftEnvironment());
        assertEquals("", params.getPartner());
        assertEquals("", params.getLongitude());
        assertEquals("", params.getPartitionId());
        assertEquals("", params.getBasePath());
        assertEquals("", params.getSerializationProvider());

        assertTrue(params.isEnvironmentInvalid());
        assertTrue(params.isAftEnvironmentInvalid());
        assertTrue(params.isLatitudeInvalid());
        assertTrue(params.isLongitudeInvalid());
        assertTrue(params.isPartnerInvalid());
        assertTrue(params.isServersInvalid());
        assertTrue(params.isPartitionIdInvalid());
        assertTrue(params.isApiKeyValid());
        assertTrue(params.isApiSecretValid());
        assertTrue(params.isUserNameValid());
        assertTrue(params.isPasswordValid());
        assertTrue(params.isAdditionalPropsValid());
        assertFalse(params.isConsumerInstanceInvalid());
        assertFalse(params.isConsumerGroupInvalid());
        assertFalse(params.isClientNameInvalid());
        assertFalse(params.isTopicInvalid());
        assertFalse(params.isHostnameInvalid());
        assertFalse(params.isPortInvalid());
    }


}
