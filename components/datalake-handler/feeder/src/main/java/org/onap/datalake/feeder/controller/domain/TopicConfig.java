/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2019 QCT
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

package org.onap.datalake.feeder.controller.domain;

import lombok.Getter;
import lombok.Setter;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.repository.DbRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * JSON request body for Topic manipulation.
 *
 * @author Kate Hsuan
 *
 */

@Getter
@Setter

public class TopicConfig {

    private String name;
    private String login;
    private String password;
    private List<String> sinkdbs;
    private boolean enable;
    private boolean save_raw;
    private String data_format;
    private int ttl;
    private boolean correlated_clearred_message;
    private String message_id_path;



}
