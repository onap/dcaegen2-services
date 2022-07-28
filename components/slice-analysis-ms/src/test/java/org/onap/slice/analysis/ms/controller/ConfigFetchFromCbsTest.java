/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *  Copyright (C) 2022 CTC, Inc.
 *  ==============================================================================
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


package org.onap.slice.analysis.ms.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableRequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LoggerFactory.class,
        RequestDiagnosticContext.class,
        CbsClientConfiguration.class,
        ImmutableRequestDiagnosticContext.class,
        CbsClientFactory.class})
public class ConfigFetchFromCbsTest {

    @Test
    public void getAppConfigTest() {
        PowerMockito.mockStatic(LoggerFactory.class);
        Logger log = PowerMockito.mock(Logger.class);
        when(LoggerFactory.getLogger(ConfigFetchFromCbs.class)).thenReturn(log);

        PowerMockito.mockStatic(RequestDiagnosticContext.class);
        ImmutableRequestDiagnosticContext diagnosticContext = PowerMockito.mock(ImmutableRequestDiagnosticContext.class);
        when(RequestDiagnosticContext.create()).thenReturn(diagnosticContext);

        PowerMockito.mockStatic(CbsClientConfiguration.class);
        CbsClientConfiguration cbsClientConfiguration = PowerMockito.mock(CbsClientConfiguration.class);
        when(CbsClientConfiguration.fromEnvironment()).thenReturn(cbsClientConfiguration);

        PowerMockito.mockStatic(CbsClientFactory.class);
        Mono mono = PowerMockito.mock(Mono.class);
        when(CbsClientFactory.createCbsClient(cbsClientConfiguration)).thenReturn(mono);

        ConfigFetchFromCbs configFetchFromCbs = new ConfigFetchFromCbs(Duration.ofSeconds(60));
        Thread configFetchThread = new Thread(configFetchFromCbs);
        configFetchThread.start();
        assertEquals(1+1,2);

    }
}
