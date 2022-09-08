/*
 * ============LICENSE_START=======================================================
 * BBS-RELOCATION-CPE-AUTHENTICATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.bbs.event.processor.utilities;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

public class LoggingUtil {

    private static final String BBS_EP_APPLICATION_PACKAGE = "org.onap.bbs";
    private static final Logger applicationLogger = (Logger) LoggerFactory.getLogger(BBS_EP_APPLICATION_PACKAGE);
    private static final List<String> loggingLevels =
            Arrays.asList("OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE");

    // Non-instantiable class
    private LoggingUtil() {
    }

    /**
     * Change Application logging level.
     * @param level logging level. Must be one of OFF, ERROR, WARN, INFO, DEBUG, TRACE
     * @return Flag indicating if it changed the level or not
     */
    public static boolean changeLoggingLevel(String level) {
        if (loggingLevels.contains(level)) {
            applicationLogger.setLevel(Level.toLevel(level));
            return true;
        }
        return false;
    }
}
