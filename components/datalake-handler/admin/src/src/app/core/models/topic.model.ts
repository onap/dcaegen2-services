/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2019 - 2020 QCT
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
  public countsDb: CountsDb;
  public countsKafka: number;
  // properties only for UI
  public config: boolean; //true: Configure, otherwise false: Unconfiure
  public countsMONGO: number;
  public countsDRUID: number;
  public countsHDFS: number;
  public countsES: number;
  public countsCB: number;
}

class CountsDb {
  MONGO: number;
  DRUID: number;
  HDFS: number;
  ES: number;
  CB: number;
}
