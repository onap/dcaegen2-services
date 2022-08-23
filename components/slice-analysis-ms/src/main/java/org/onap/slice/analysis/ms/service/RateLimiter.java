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
package org.onap.slice.analysis.ms.service;

/**
 * A simple rate-limiter; make sure bandwidth adjustment requests don't swarm underlaying network controller
 */
class RateLimiter {
    private int MAX_TOKENS;
    private long lastRequestTime = System.currentTimeMillis();
    private long possibleTokens = 0;
    private long interval = 1000;

    /**
     * Constructor for rate limiter (simple token bucket filter)
     * @param maxTokens max number of token allowed
     * @param interval interval(ms) between received new token
     */
    public RateLimiter(int maxTokens, int interval){
        MAX_TOKENS = maxTokens;
        this.interval = interval;
    }

    /**
     * Trying to get a new token for execution, if no token left, stall for interval ms.
     * @throws InterruptedException
     */
    synchronized public void getToken() throws InterruptedException {
        possibleTokens += (System.currentTimeMillis() - lastRequestTime) / interval;
        if (possibleTokens > MAX_TOKENS){
            possibleTokens = MAX_TOKENS;
        }
        if (possibleTokens == 0){
            Thread.sleep(interval);
        } else {
            possibleTokens--;
        }
        // granting token
        lastRequestTime = System.currentTimeMillis();
    }
}
