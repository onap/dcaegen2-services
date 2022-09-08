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

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

/**
 * test json utils
 *
 * @author Guobiao Mo
 */
public class JsonUtilTest {

	@Test
	public void arrayAggregate() {
		String text = "{a:{b:[{c:1, d: vvvv},{c:2, d: xxxx, f:6.9}]}}";
		JSONObject json = new JSONObject(text);

		JsonUtil.arrayAggregate("/a/b", json);
		String expected = "{\"a\":{\"b\":[{\"c\":1,\"d\":\"vvvv\"},{\"c\":2,\"d\":\"xxxx\",\"f\":6.9}],\"b_count\":2,\"b_min\":{\"f\":6.9,\"c\":1},\"b_max\":{\"f\":6.9,\"c\":2},\"b_sum\":{\"f\":6.9,\"c\":3},\"b_average\":{\"f\":3.45,\"c\":1.5}}}";
		assertEquals(expected, json.toString());

		JsonUtil.arrayAggregate("/a/bxx", json); 

	}

	@Test
	public void flattenArray() {
		String text = "{a:{b:[{c:1, d: vvvv},{c:2, d: xxxx, f:6.9}]}}";
		JSONObject json = new JSONObject(text);
 
		JsonUtil.flattenArray("/a/b/d", json);
		System.out.println(json.toString());
		String expected = "{\"a\":{\"b_d_vvvv\":{\"c\":1,\"d\":\"vvvv\"},\"b\":[{\"c\":1,\"d\":\"vvvv\"},{\"c\":2,\"d\":\"xxxx\",\"f\":6.9}],\"b_d_xxxx\":{\"c\":2,\"d\":\"xxxx\",\"f\":6.9}}}";
		assertEquals(expected, json.toString());

		JsonUtil.flattenArray("/a/bxx", json);

	}

}
