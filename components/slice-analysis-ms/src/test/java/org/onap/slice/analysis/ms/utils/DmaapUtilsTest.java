/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *  Copyright (C) 2020-2022 Wipro Limited.
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
package org.onap.slice.analysis.ms.utils;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.slice.analysis.ms.models.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DmaapUtils.class, loader = AnnotationConfigContextLoader.class)
public class DmaapUtilsTest {
	@Test
	public void builder1() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		DmaapUtils dmaapUtils = new DmaapUtils();
		Configuration config = Mockito.mock(Configuration.class);
		Method builder = DmaapUtils.class.getDeclaredMethod("builder", Configuration.class, String.class);
		builder.setAccessible(true);
		builder.invoke(dmaapUtils, config, "topic");
		assertEquals(false, config.isSecured());

	}

	@Test
	public void authenticatedBuilder1() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		DmaapUtils dmaapUtils = new DmaapUtils();
		Configuration config = Mockito.mock(Configuration.class);
		Method authenticatedBuilder = DmaapUtils.class.getDeclaredMethod("authenticatedBuilder", Configuration.class,
				String.class);
		authenticatedBuilder.setAccessible(true);
		authenticatedBuilder.invoke(dmaapUtils, config, "topic");
		assertEquals(null, config.getAafUsername());
		assertEquals(null, config.getAafPassword());

	}

	@Test
	public void unAuthenticatedBuilder1() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		DmaapUtils dmaapUtils = new DmaapUtils();
		Configuration config = Mockito.mock(Configuration.class);
		Method unAuthenticatedBuilder = DmaapUtils.class.getDeclaredMethod("unAuthenticatedBuilder",
				Configuration.class, String.class);
		unAuthenticatedBuilder.setAccessible(true);
		unAuthenticatedBuilder.invoke(dmaapUtils, config, "topic");
		assertEquals(false, config.isSecured());

	}

	@Test
	public void builderConsumer1() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		DmaapUtils dmaapUtils = new DmaapUtils();
		Configuration config = Mockito.mock(Configuration.class);
		Method builderConsumer = DmaapUtils.class.getDeclaredMethod("builderConsumer", Configuration.class,
				String.class);
		builderConsumer.setAccessible(true);
		builderConsumer.invoke(dmaapUtils, config, "topic");
		assertEquals(false, config.isSecured());
	}

	@Test
	public void unAuthenticatedConsumerBuilder1() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		DmaapUtils dmaapUtils = new DmaapUtils();
		Configuration config = Mockito.mock(Configuration.class);
		Method unAuthenticatedConsumerBuilder = DmaapUtils.class.getDeclaredMethod("unAuthenticatedConsumerBuilder",
				Configuration.class, String.class);
		unAuthenticatedConsumerBuilder.setAccessible(true);
		unAuthenticatedConsumerBuilder.invoke(dmaapUtils, config, "topic");
		assertEquals(null, config.getAafUsername());
		assertEquals(null, config.getAafPassword());

	}

	@Test
	public void authenticatedConsumerBuilder1() throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		DmaapUtils dmaapUtils = new DmaapUtils();
		Configuration config = Mockito.mock(Configuration.class);
		Method authenticatedConsumerBuilder = DmaapUtils.class.getDeclaredMethod("authenticatedConsumerBuilder",
				Configuration.class, String.class);
		authenticatedConsumerBuilder.setAccessible(true);
		authenticatedConsumerBuilder.invoke(dmaapUtils, config, "topic");
		assertEquals(null, config.getAafUsername());
		assertEquals(null, config.getAafPassword());
	}

}
