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

import { Component, OnInit, Input, ViewChild } from "@angular/core";
import { NgxSpinnerService } from "ngx-spinner";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { RestApiService } from "src/app/core/services/rest-api.service";
import { AdminService } from "src/app/core/services/admin.service";
import { Db, DbType } from "src/app/core/models/db.model";
import { from, forkJoin } from "rxjs";
import { mergeMap, map } from "rxjs/operators";

@Component({
  selector: "app-db-modal",
  templateUrl: "./db-modal.component.html",
  styleUrls: ["./db-modal.component.css"]
})
export class DbModalComponent implements OnInit {
  @Input() data: Db;
  @Input() mode: string;

  dbTypes: Array<DbType> = [];

  constructor(
    public activeModal: NgbActiveModal,
    public adminService: AdminService,
    private restApiService: RestApiService,
    private spinner: NgxSpinnerService
  ) {}

  ngOnInit() {
    // this.spinner.show();

    // get database types
    const get_dbTypes = this.restApiService.getDbTypes().pipe(
      mergeMap(dbTypes => from(dbTypes)),
      map(dbType => {
        if (!dbType.tool) this.dbTypes.push(dbType);
      })
    );

    forkJoin(get_dbTypes).subscribe(data => {
      if (this.mode === "new") {
        // default value
        if (this.dbTypes.length > 0) {
          this.data.dbTypeId = this.dbTypes[0].id;
        }
      }
    });
  }
}
