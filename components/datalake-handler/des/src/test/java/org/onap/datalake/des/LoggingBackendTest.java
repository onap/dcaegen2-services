/*-
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE DES
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.datalake.des;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.SLF4JServiceProvider;

import ch.qos.logback.classic.LoggerContext;

/**
 * Verify that Logback is the only SLF4J provider on the classpath.
 */
public class LoggingBackendTest {

    @Test
    public void onlyOneSlf4jProviderIsOnTheClasspath() {
        List<String> providers = ServiceLoader.load(SLF4JServiceProvider.class).stream()
                .map(provider -> provider.type().getName()).toList();
        assertEquals("SLF4J providers found: " + providers, 1, providers.size());
    }

    @Test
    public void slf4jIsBoundToLogback() {
        assertTrue("SLF4J is bound to " + LoggerFactory.getILoggerFactory().getClass().getName(),
                LoggerFactory.getILoggerFactory() instanceof LoggerContext);
    }
}
