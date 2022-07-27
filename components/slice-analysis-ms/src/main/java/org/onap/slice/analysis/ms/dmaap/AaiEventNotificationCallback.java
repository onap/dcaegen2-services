/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *  Copyright (C) 2022 Huawei Canada Limited.
 *  Modifications Copyright (C) 2022 CTC, Inc.
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

package org.onap.slice.analysis.ms.dmaap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.service.ccvpn.BandwidthEvaluator;
import org.onap.slice.analysis.ms.service.ccvpn.Event;
import org.onap.slice.analysis.ms.service.ccvpn.SimpleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Handles AAI-EVENT from dmaap
 */
@Component
public class AaiEventNotificationCallback implements NotificationCallback {

    private static Logger logger = LoggerFactory.getLogger(AaiEventNotificationCallback.class);
    private static final String EVENT_HEADER = "event-header";
    private static final String ACTION = "action";
    private static final String ENTITY_TYPE = "entity-type";
    private static final String SOURCE_NAME = "source-name";
    private static final String ENTITY = "entity";
    private final JsonParser parser = new JsonParser();
    private Configuration configuration;
    private String aaiNotifTargetAction;
    private String aaiNotifTargetSource;
    private String aaiNotifTargetEntity;

    @Autowired
    BandwidthEvaluator bandwidthEvaluator;

    @PostConstruct
    public void init(){
        configuration = Configuration.getInstance();
        aaiNotifTargetAction = configuration.getAaiNotifTargetAction();
        aaiNotifTargetSource = configuration.getAaiNotifTargetSource();
        aaiNotifTargetEntity = configuration.getAaiNotifTargetEntity();
    }

    @Override
    public void activateCallBack(String msg) {
        handleNotification(msg);
    }

    private void handleNotification(String msg) {
        JsonElement jsonElement = parser.parse(msg);
        if (jsonElement.isJsonObject()){
            //handle a single AAI_EVENT
            logger.debug("Handle a single aai-event");
            handleMsgJsonObject(jsonElement.getAsJsonObject());
        } else if (jsonElement.isJsonArray()){
            //handle a series of AAI_EVENT
            logger.debug("Handle a series of aai-event");
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (int i=0,e=jsonArray.size(); i<e; i++){
                if (jsonArray.get(i).isJsonPrimitive()){
                    // Deal with a batch of event message
                    handleNotification(jsonArray.get(i).getAsString());
                } else {
                    handleMsgJsonObject(jsonArray.get(i).getAsJsonObject());
                }

            }
        }
    }

    private void handleMsgJsonObject(JsonObject jsonObject){
        JsonObject header = jsonObject.get(EVENT_HEADER).getAsJsonObject();
        if (!header.has(ACTION) || !header.get(ACTION).getAsString().equals(aaiNotifTargetAction)){
            return;
        }
        if (!header.has(ENTITY_TYPE) || !header.get(ENTITY_TYPE).getAsString().equals(aaiNotifTargetEntity)){
            return;
        }
        if (!header.has(SOURCE_NAME) || !header.get(SOURCE_NAME).getAsString().equals(aaiNotifTargetSource)){
            return;
        }
        JsonObject entity = jsonObject.get(ENTITY).getAsJsonObject();
        JsonObject body = getNestedJsonObject(entity, aaiNotifTargetEntity);
        logger.info("AAI-EVENT entity object {}", body);
        if (body == null){
            return;
        }
        Event event = new SimpleEvent<>(SimpleEvent.Type.ONDEMAND_CHECK, body);
        bandwidthEvaluator.post(event);
    }

    private JsonObject getNestedJsonObject(JsonObject obj, String target){
        for (String k: obj.keySet()){
            if (k.equals(target)){
                //Found it;
                return obj.getAsJsonArray(k).get(0).getAsJsonObject();
            }
            if (obj.get(k).isJsonObject()) {
                return getNestedJsonObject(obj.getAsJsonObject(k), target);
            } else if (obj.get(k).isJsonArray()){
                JsonElement tmp = obj.getAsJsonArray(k).get(0);
                if (tmp.isJsonObject()){
                    return getNestedJsonObject(tmp.getAsJsonObject(), target);
                }
            }
        }
        return null;
    }
}
