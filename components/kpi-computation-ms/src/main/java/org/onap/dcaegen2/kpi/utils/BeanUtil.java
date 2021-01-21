/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 China Mobile.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.kpi.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * This class is used to get/set bean references.
 *
 * @author Kai Lu
 */
@Service
public class BeanUtil implements ApplicationContextAware {
    private static ApplicationContext context;

    /**
     * set ApplicationContext.
     *
     * @param applicationContext applicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    /**
     * Get bean class.
     * 
     * @param <T>       T
     * @param beanClass beanClass
     * @return T
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}
