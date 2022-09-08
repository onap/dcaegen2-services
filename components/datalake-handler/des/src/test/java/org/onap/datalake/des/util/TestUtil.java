/*
 * ============LICENSE_START=======================================================
 * ONAP : DCAE
 * ================================================================================
 * Copyright 2020 China Mobile
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

package org.onap.datalake.des.util;
 
import org.onap.datalake.des.domain.Db;
import org.onap.datalake.des.domain.DbType;
 

/**
 * test utils.
 *
 * @author Kai Lu
 */
public class TestUtil {

    static int i = 0;

    /**
     * getDataExposureConfig.
     *
     * @param name name
     * @return data exposure config
     *
     */
    public static Db newDb(String name) {
        Db db = new Db();
        db.setId(i++);
        db.setName(name);   
        db.setDbType(new DbType(name, name));
        return db;
    }
}
