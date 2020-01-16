/*
* ============LICENSE_START=======================================================
* ONAP : DataLake
* ================================================================================
* Copyright 2019-2020 China Mobile
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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * SpringJdbcService
 *
 * @author Kai Lu
 *
 */
public class SpringJdbcService {

    private static Logger logger = LoggerFactory.getLogger(SpringJdbcService.class);

    /**
    *
    * prestoMongodbJdbcTemplate
    *
    * @param jdbcTemplate jdbcTemplate
    * @param sql sql query
    * @param params sql params
    * 
    * @return JSONArray json array
    *
    */
    public JSONArray getJSONArray(JdbcTemplate jdbcTemplate, String sql, Object[] params) {
        return getJSONArray(jdbcTemplate, sql, params, false);
    }

    /**
    *
    * prestoMongodbJdbcTemplate
    *
    * @param jdbcTemplate jdbcTemplate
    * @param sql sql query
    * @param params sql params
    * @param toUpper yes or no
    * 
    * @return JSONArray json array
    *
    */
    public JSONArray getJSONArray(JdbcTemplate jdbcTemplate, String sql, Object[] params, final boolean toUpper) {

        logger.info("get sql:" + sql);
        if (params != null && params.length > 0) {
            for (Object o : params) {
                logger.info("value:" + o);
            }
        }

        return jdbcTemplate.query(sql, params, new ResultSetExtractor<JSONArray>() {
            @Override
            public JSONArray extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                ResultSetMetaData rsd = resultSet.getMetaData();
                int clength = rsd.getColumnCount();
                JSONArray ja = new JSONArray();
                String columnName;
                try {
                    while (resultSet.next()) {
                        JSONObject jo = new JSONObject();

                        for (int i = 0; i < clength; i++) {
                            columnName = rsd.getColumnLabel(i + 1);
                            columnName = toUpper ? columnName.toUpperCase() : columnName.toLowerCase();
                            jo.put(columnName, resultSet.getObject(i + 1));
                        }
                        ja.put(jo);
                    }
                } catch (Exception e) {

                }
                return ja;
            }
        });

    }

    /**
    *
    * getJSONObject
    *
    * @param jdbcTemplate jdbcTemplate
    * @param sql sql query
    * 
    * @return JSONObject json object
    *
    */
    public static JSONObject getJSONObject(JdbcTemplate jdbcTemplate, String sql) {
        return getJSONObject(jdbcTemplate, sql, new Object[]{});
    }

    /**
    *
    * getJSONObject
    *
    * @param jdbcTemplate jdbcTemplate
    * @param sql sql query
    * @param params params
    *
    * @return JSONObject json object
    *
    */
    public static JSONObject getJSONObject(JdbcTemplate jdbcTemplate, String sql, Object[] params) {
        return getJSONObject(jdbcTemplate, sql, params, false);
    }

    /**
    *
    * getJSONObject
    *
    * @param jdbcTemplate jdbcTemplate
    * @param sql sql query
    * @param params params
    * @param toUpper yes or to
    *
    * @return JSONObject json object
    *
    */
    public static JSONObject getJSONObject(JdbcTemplate jdbcTemplate, String sql, Object[] params, final boolean toUpper) {
        logger.info("save sql:" + sql);
        if (params != null && params.length > 0) {
            for (Object o : params) {
                logger.info("value:" + o);
            }
        }
        return jdbcTemplate.query(sql, params, new ResultSetExtractor<JSONObject>() {
            @Override
            public JSONObject extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                ResultSetMetaData rsd = resultSet.getMetaData();
                int clength = rsd.getColumnCount();
                String columnName;
                try {
                    if (resultSet.next()) {
                        JSONObject jo = new JSONObject();

                        for (int i = 0; i < clength; i++) {
                            columnName = rsd.getColumnLabel(i + 1);
                            columnName = toUpper ? columnName.toUpperCase() : columnName.toLowerCase();
                            jo.put(columnName, resultSet.getObject(i + 1));
                        }
                        return jo;
                    }
                } catch (Exception e) {

                }
                return null;
            }
        });

    }

}