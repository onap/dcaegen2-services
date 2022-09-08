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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * utils
 * 
 * @author Guobiao Mo
 *
 */
public class Util {

	// https://commons.apache.org/proper/commons-io/description.html
	public static String getTextFromFile(String fileName) throws IOException {
		File file = new File(fileName);
		String string = FileUtils.readFileToString(file, "UTF-8");
		return string;
	}

	/**
	 * given a json file, remove dot(.) in all keys
	 * http://www.vogella.com/tutorials/JavaRegularExpressions/article.html
	 */
	public static String replaceDotInKey(String json) {
		String regex = "(\"[\\-\\w]+)(\\.)([\\.\\-\\w]+\\\\?\"\\:)";

		String newJson = json.replaceAll(regex, "$1_$3");

		if (json.equals(newJson)) {
			return json;
		} else {
			return replaceDotInKey(newJson);// there maybe more to replace
		}
	}
	
	public static boolean isStall(long lastTime, long checkInterval) {
		return System.currentTimeMillis() > lastTime + checkInterval;
	}
}
