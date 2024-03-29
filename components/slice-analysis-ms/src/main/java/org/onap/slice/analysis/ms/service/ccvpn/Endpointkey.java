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

import java.util.Objects;

/**
 * Endpoint key class represents each uni associated with each cll
 */
public class Endpointkey {

    private final String cllId;
    private final String uniId;

    /**
     * Constructor accpets cllId and uniId.
     * @param cllId String cll instance id
     * @param uniId String uni id
     */
    public Endpointkey(String cllId, String uniId){
        this.cllId = cllId;
        this.uniId = uniId;
    }

    /**
     * Return cllId
     * @return String cllId
     */
    public String getCllId() {
        return cllId;
    }

    /**
     * Return uniId
     * @return String uni id
     */
    public String getUniId() {
        return uniId;
    }

    @Override
    public int hashCode() { return Objects.hash(cllId, uniId); }

    @Override
    public boolean equals(Object obj){
        if (this == obj){
            return true;
        }
        if (obj instanceof Endpointkey){
            final Endpointkey other = (Endpointkey) obj;
            return Objects.equals(this.cllId, other.cllId) &&
                    Objects.equals(this.uniId, other.uniId);
        }
        return false;
    }

}
