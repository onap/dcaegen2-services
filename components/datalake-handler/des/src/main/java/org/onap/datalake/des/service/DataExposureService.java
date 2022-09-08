/*
* ============LICENSE_START=======================================================
* ONAP : Data Extraction Service
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

package org.onap.datalake.des.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.onap.datalake.des.domain.DataExposure;
import org.onap.datalake.des.domain.Db;
import org.onap.datalake.des.dto.DataExposureConfig;
import org.onap.datalake.des.repository.DataExposureRepository;
import org.onap.datalake.des.repository.DbRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for DataExposure.
 *
 * @author Kai Lu
 *
 */
@Service
public class DataExposureService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private DataExposureRepository dataExposureRepository;
    @Autowired
    private DbRepository dbRepository;

    /**
     * getDataExposure.
     *
     * @param serviceId serviceId
     *
     * @return DataExposure
     *
     */
    public DataExposure getDataExposure(String serviceId) {
        Optional<DataExposure> ret = dataExposureRepository.findById(serviceId);
        return ret.isPresent() ? ret.get() : null;
    }

    /**
     * getDataExposure.
     *
     * @return DataExposure
     *
     */
    public List<DataExposureConfig> queryAllDataExposure() {
        List<DataExposure> dataExposureList = null;
        List<DataExposureConfig> dataExposureConfigList = new ArrayList<>();
        dataExposureList = (List<DataExposure>) dataExposureRepository.findAll();
        if (!dataExposureList.isEmpty()) {
            log.info("DataExposureList is not null");
            for (DataExposure dataExposure : dataExposureList) {
                dataExposureConfigList.add(dataExposure.getDataExposureConfig());
            }
        }
        return dataExposureConfigList;
    }

    /**
     * getDataExposureById.
     *
     * @param id id
     *
     * @return data exposure
     *
     */
    public DataExposure getDataExposureById(String id) {
        Optional<DataExposure> ret = dataExposureRepository.findById(id);
        return ret.isPresent() ? ret.get() : null;
    }

    /**
     * fillDataExposureConfiguration.
     *
     * @param dataExposureConfig DataExposureConfig
     *
     * @return data exposure
     *
     */
    public DataExposure fillDataExposureConfiguration(DataExposureConfig dataExposureConfig) {
        DataExposure dataExposure = new DataExposure();
        fillDataExposure(dataExposureConfig, dataExposure);
        return dataExposure;
    }

    /**
     * fillDataExposureConfiguration.
     *
     * @param dataExposureConfig DataExposureConfig
     * @param dataExposure       DataExposure
     *
     * @return data exposure
     *
     */
    public void fillDataExposureConfiguration(DataExposureConfig dataExposureConfig, DataExposure dataExposure) {
        fillDataExposure(dataExposureConfig, dataExposure);
    }

    private void fillDataExposure(DataExposureConfig dataExposureConfig, DataExposure dataExposure)
            throws IllegalArgumentException {

        dataExposure.setId(dataExposureConfig.getId());
        dataExposure.setNote(dataExposureConfig.getNote());
        dataExposure.setSqlTemplate(dataExposureConfig.getSqlTemplate());
        if (dataExposureConfig.getDbId() == null) {
            throw new IllegalArgumentException("Can not find db_id in db, db_id: " + dataExposureConfig.getDbId());
        }

        Optional<Db> dbOptional = dbRepository.findById(dataExposureConfig.getDbId());

        if (!dbOptional.isPresent()) {
            throw new IllegalArgumentException("db_id is null " + dataExposureConfig.getDbId());
        }

        dataExposure.setDb(dbOptional.get());
    }
}
