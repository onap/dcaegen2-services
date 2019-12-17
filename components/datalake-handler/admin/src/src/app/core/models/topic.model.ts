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

/**
 *
 * @author Ekko Chang
 *
 */

export class Topic {
  public id: number;
  public name: string;
  public login: string;
  public password: string;
  public enabledSinkdbs: Array<string>; // related db which is enabled
  public sinkdbs: Array<string>; // related db whatever enabled or disable
  public enabled: boolean;
  public saveRaw: boolean;
  public dataFormat: string;
  public ttl: number;
  public correlateClearedMessage: boolean;
  public messageIdPath: string;
  public aggregateArrayPath: string;
  public flattenArrayPath: string;
  public enbabledKafkas: Array<string>;
  public kafkas: Array<string>;
  // properties only for UI
  public config: boolean; //true: Configure, otherwise false: Unconfiure
  public kafkaName: string;
  public countCouchbase: number;
  public countDruid: number;
  public countEs: number;
  public countMongo: number;
  public countHadoop: number;

  constructor(
    id: number,
    name: string,
    login: string,
    password: string,
    enabledSinkdbs: Array<string>,
    sinkdbs: Array<string>,
    enabled: boolean,
    saveRaw: boolean,
    dataFormat: string,
    ttl: number,
    correlateClearedMessage: boolean,
    messageIdPath: string,
    aggregateArrayPath: string,
    flattenArrayPath: string,
    enbabledKafkas: Array<string>,
    kafkas: Array<string>,
    config: boolean
  ) {
    this.id = id;
    this.name = name;
    this.login = login;
    this.password = password;
    this.enabledSinkdbs = enabledSinkdbs;
    this.sinkdbs = sinkdbs;
    this.enabled = enabled;
    this.saveRaw = saveRaw;
    this.dataFormat = dataFormat;
    this.ttl = ttl;
    this.correlateClearedMessage = correlateClearedMessage;
    this.messageIdPath = messageIdPath;
    this.aggregateArrayPath = aggregateArrayPath;
    this.flattenArrayPath = flattenArrayPath;
    this.enbabledKafkas = enbabledKafkas;
    this.kafkas = kafkas;
    this.config = config;
  }
}
