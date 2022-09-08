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

export class Db {
  public id: number;
  public name: string;
  public enabled: boolean;
  public host: string;
  public port: number;
  public database: string;
  public encrypt: boolean;
  public login: string;
  public pass: string;
  public dbTypeId: string;
  // for UI display
  public checkedToSave: boolean;
}

export class DbType {
  public id: string;
  public name: string;
  public defaultPort: number;
  public tool: boolean;
}
