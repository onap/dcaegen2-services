/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Huawei Canada Limited.
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
package org.onap.slice.analysis.ms.service.ccvpn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StrategyFactory {
    private static Logger log = LoggerFactory.getLogger(StrategyFactory.class);

    @Autowired
    public List<EvaluationStrategy> strategies;

    private StrategyFactory() {}

    /**
     * Get evulation strategy by name
     * @param name evaluationStrategy name
     * @return EvaluationStrategy
     */
    public EvaluationStrategy getStrategy(String name){
        if (null == name || name.isEmpty()){
            log.error("Empty strategy name provided in config file");
            throw new IllegalArgumentException("Unknown strategy name: " + name);
        }
        for(EvaluationStrategy s: strategies){
            if(s.getName().equals(name)){
                return s;
            }
        }
        log.error("Unknown strategy name: {}", name);
        throw new IllegalArgumentException("Unknown strategy name: " + name);
    }
}
