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
 * @contributor Chunmeng Guo
 *
 */

import { Component, OnInit, Input, Output, EventEmitter } from "@angular/core";

@Component({
  selector: "app-table",
  templateUrl: "./table.component.html",
  styleUrls: ["./table.component.css"]
})
export class TableComponent implements OnInit {
  @Input() columns: Array<any> = [];
  @Input() data: Array<any> = [];
  @Output() btnTableAction = new EventEmitter<object>();
  loadingIndicator: boolean = true;
  template_list: Array<any> = [];

  mesgNoData = {
    emptyMessage: `
      <div class="d-flex justify-content-center">
        <div class="p-2">
          <label class="dl-nodata">No data</label>
        </div>
      </div>
    `
  };

  constructor() {}

  ngOnInit() {
    setTimeout(() => {
      this.loadingIndicator = false;
    }, 500);
  }

  tableAction(action: string, id: number | string) {
    let passValueArr: Array<any> = [];
    passValueArr.push(action);
    passValueArr.push(id);

    // array[0] for action
    // array[1] for value
    console.log("tableAction");
    this.btnTableAction.emit(passValueArr);
  }

  rowOnActivate(event: any) {
    const emitType = event.type;

    if (emitType == "dblclick") {
      let passValueArr: Array<any> = [];
      passValueArr.push("edit");
      passValueArr.push(event.row.id);

      // // array[0] for action
      // // array[1] for value
      this.btnTableAction.emit(passValueArr);
    }
  }
}
