/*
 * ============LICENSE_START=======================================================
 * ONAP : DCAE
 * ================================================================================
 * Copyright 2019 China Mobile
 *=================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.datalake.feeder.util;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * test utils
 *
 * @author Guobiao Mo
 */
public class UtilTest {

	@Test
	//only dot(.) in key got replaced
	public void replaceDotInKey() {
		new Util();

		String a = "\"u-y.t.y-t\":\"u.gfh\",\\\"jg.h\\\":\"j_9889\"";
		String b = "\"u-y_t_y-t\":\"u.gfh\",\\\"jg_h\\\":\"j_9889\"";

		assertEquals(Util.replaceDotInKey(a), b);
	}

	@Test(expected = IOException.class)
	public void validateNull() throws IOException {
		Util.getTextFromFile("no_such_file");
	}

	@Test
	//only dot(.) in key got replaced
	public void isStall() {
		long lastTime = 10L;
		long checkInterval = 10000L;

		assertTrue(Util.isStall(lastTime, checkInterval));

		lastTime = System.currentTimeMillis();
		assertFalse(Util.isStall(lastTime, checkInterval));
	}

}
