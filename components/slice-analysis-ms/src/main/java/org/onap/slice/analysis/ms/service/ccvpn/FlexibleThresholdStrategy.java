/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Huawei Canada Limited.
 *   Copyright (C) 2022 Huawei Technologies Co., Ltd.
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

import com.google.gson.JsonObject;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.service.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class FlexibleThresholdStrategy implements EvaluationStrategy{
    private static Logger log = LoggerFactory.getLogger(FixedUpperBoundStrategy.class);
    private Configuration configuration;

    private static final String TYPE_NAME = "FlexibleThresholdStrategy";
    private static final String SERVICE_INSTANCE_LOCATION_ID = "service-instance-location-id";
    private static final String BANDWIDTH_TOTAL = "bandwidth-total";

    /**
     * Percentage threshold of bandwidth adjustment.
     */
    private static double threshold;

    /**
     * Precision of bandwidth evaluation and adjustment.
     */
    private static double precision; // in Mbps;

    @Autowired
    BandwidthEvaluator bandwidthEvaluator;

    @Autowired
    CCVPNPmDatastore ccvpnPmDatastore;

    @Autowired
    PolicyService policyService;

    @PostConstruct
    public void init() {
        loadConfig();
    }

    @Override
    public void execute(Event event){
        if (event.type() == SimpleEvent.Type.PERIODIC_CHECK && isPeriodicCheckOn()){
            log.debug("=== Processing new periodic check request: {} ===", event.time());
            Map<Endpointkey, CCVPNPmDatastore.EvictingQueue<Integer>> usedBwMap = ccvpnPmDatastore.getUsedBwMap();
            Map<String, Integer> candidate = new TreeMap<>();
            for(Map.Entry<Endpointkey, CCVPNPmDatastore.EvictingQueue<Integer>> entry: usedBwMap.entrySet()) {
                String serviceId = entry.getKey().getCllId();
                Object[] usedBws = entry.getValue().tryReadToArray();
                // judge whether this cll is under closed loop assurance
                if (!ccvpnPmDatastore.getClosedloopStatus(serviceId)) {
                    log.info("CCVPN Evaluator Output: service {}, closed loop bw modification is off.", serviceId);
                    continue;
                }
                if (usedBws == null) {
                    // No enough data for evaluating
                    log.debug("CCVPN Evaluator Output: service {}, not enough data to evaluate", serviceId);
                    continue;
                }
                if (ccvpnPmDatastore.getProvBwOfSvc(serviceId) == 0) {
                    // Max bandwidth not cached yet
                    log.debug("CCVPN Evaluator Output: service {}, max bandwidth not cached, wait for next round", serviceId);
                    post(new SimpleEvent(SimpleEvent.Type.AAI_BW_REQ, serviceId));
                    continue;
                }
                double avg = Arrays.stream(usedBws)
                    .mapToInt(o -> (int) o)
                    .summaryStatistics()
                    .getAverage();
                int provBw = ccvpnPmDatastore.getProvBwOfSvc(serviceId);
                int originalBw = ccvpnPmDatastore.getOriginalBw(serviceId);

                if(needIncrease(serviceId, avg, provBw)){
                    int newBw = (int) (Math.ceil((avg / threshold) * 1.2 / precision) * precision);
                    log.info("For cll {}, going to increase bw to {}", serviceId, newBw);
                    candidate.put(serviceId, Math.max(candidate.getOrDefault(serviceId, 0), newBw));
                } else {
                    if(needDecrease(serviceId, avg, provBw, originalBw)) {
                        int newBw = (int) (Math.ceil(originalBw * 0.5));
                        log.info("For cll {}, going to decrease bw to {}", serviceId, newBw);
                        candidate.put(serviceId, Math.max(candidate.getOrDefault(serviceId, 0), newBw));
                    }
                }
            }
            // check svc under maintenance
            Map<String , ServiceState> svcUnderMaintenance = getServicesUnderMaintenance();
            for (Map.Entry<String, ServiceState> entry: svcUnderMaintenance.entrySet()){
                candidate.putIfAbsent(entry.getKey(), 0);
            }
            // fetch the provisioned bandwidth info if underMaintenance; otherwise send modification request
            for(Map.Entry<String, Integer> entry: candidate.entrySet()) {
                //still doing adjustment
                String cllId = entry.getKey();
                Integer newBw = entry.getValue();
                if(!ccvpnPmDatastore.getClosedloopStatus(cllId)) {
                    log.debug("CCVPN Evaluator Output: service {} is not under closed loop assurance", cllId);
                    continue;
                }
                if (isServiceUnderMaintenance(cllId)) {
                    if (newBw == 0){
                        log.debug("CCVPN Evaluator Output: service {}," +
                            " is in maintenance state, fetching bandwidth info from AAI", cllId);
                    } else {
                        log.debug("CCVPN Evaluator Output: candidate {}," +
                            " need an adjustment, but skipped due to in maintenance state", cllId);
                    }
                    post(new SimpleEvent(SimpleEvent.Type.AAI_BW_REQ, cllId));
                    continue;
                }
                //not in the mid of adjustment; we are free to adjust.
                log.info("CCVPN Evaluator Output: candidate {}," +
                    " need an adjustment, sending request to policy", entry.getKey());
                ccvpnPmDatastore.updateSvcState(entry.getKey(), ServiceState.UNDER_MAINTENANCE);
                sendModifyRequest(entry.getKey(), newBw, RequestOwner.DCAE);
            }
            log.debug("=== Processing periodic check complete ===");
        }
        if (event.type() == SimpleEvent.Type.ONDEMAND_CHECK && isOnDemandCheckOn()) {
            log.debug("=== Processing upperbound adjustment request: {} ===", event.time());
            JsonObject payload = (JsonObject) event.subject();
            String serviceId = payload.get(SERVICE_INSTANCE_LOCATION_ID).getAsString();
            int newBandwidth = payload.get(BANDWIDTH_TOTAL).getAsInt();
            log.info("Update service {} bandwidth upperbound to {} ", serviceId, newBandwidth);
            ccvpnPmDatastore.updateUpperBoundBw(serviceId, newBandwidth);
            log.debug("=== Processing upperbound adjustment complete ===");
        }
    }

    @Override
    public String getName() {
        return TYPE_NAME;
    }

    /**
     * Post/broadcast event to the BandwidthEvaluator
     * @param event event object
     */
    private void post(Event event){
        bandwidthEvaluator.post(event);
    }

    private void loadConfig() {
        configuration = Configuration.getInstance();
        threshold = configuration.getCcvpnEvalThreshold();
        precision = configuration.getCcvpnEvalPrecision(); // in Mbps;
    }

    private boolean isPeriodicCheckOn() {
        configuration = Configuration.getInstance();
        return configuration.isCcvpnEvalPeriodicCheckOn();
    }

    private boolean isOnDemandCheckOn() {
        configuration = Configuration.getInstance();
        return configuration.isCcvpnEvalOnDemandCheckOn();
    }

    // send modification requestion
    private void sendModifyRequest(String cllId, Integer newBandwidth, RequestOwner owner) {
        log.info("Sending modification request to policy. RequestOwner: {} - Service: {} change to bw: {}",
            owner, cllId, newBandwidth);
        policyService.sendOnsetMessageToPolicy(
            policyService.formPolicyOnsetMessageForCCVPN(cllId, newBandwidth, owner)
        );
    }

    private boolean needIncrease(String serviceId, double currAvgUsage, int provBw) {
        log.info("cll {} judge whether increase", serviceId);
        if ( currAvgUsage > threshold * provBw ) {
            log.info("decide to increase, original bw {}, currAvg bw {}, maxBw {}", currAvgUsage, provBw);
            return true;
        }
        return false;
    }

    private boolean needDecrease(String serviceId, double currAvgUsage, int provBw, int originalBw) {
        log.info("cll {} judge whether decrease", serviceId);
        if( currAvgUsage < 0.3 * originalBw) {
            log.info("decide to decrease, original bw {}, currAvg bw {}, prov {}", originalBw, currAvgUsage, provBw);
            return true;
        }
        return false;
    }
    // check is service under maint
    private boolean isServiceUnderMaintenance(String serivceId) {
        return ccvpnPmDatastore.getStatusOfSvc(serivceId) == ServiceState.UNDER_MAINTENANCE;
    }
    // get a collection of service under maint
    private Map<String, ServiceState> getServicesUnderMaintenance(){
        return ccvpnPmDatastore.getSvcStatusMap().entrySet()
            .stream()
            .filter(e -> e.getValue() == ServiceState.UNDER_MAINTENANCE)
            .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
    }
}
