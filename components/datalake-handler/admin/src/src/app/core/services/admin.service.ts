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
 * This service is to set the page title from different components.
 *
 * @author Ekko Chang
 *
 */

import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";

@Injectable()
export class AdminService {
  public title = new BehaviorSubject("Title");

  constructor() {}

  /*
   *  Set header title
   */
  setTitle(title: string) {
    this.title.next(title);
  }

  /*
   *  Form validate
   */
  onKeyPressNumber(data: any) {
    return (data.target.value = data.target.value.replace(/[^0-9.]/g, ""));
  }

  onKeyPressSymbol(data: any) {
    return (data.target.value = data.target.value.replace(
      /[~`!#$%\^&*+=\-\[\]\\';,/{}()|\\":<>\?@.]/g,
      ""
    ));
  }
}
