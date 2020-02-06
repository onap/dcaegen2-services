/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2020 QCT
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

import { Component, OnInit, Input } from "@angular/core";

import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { RestApiService } from "src/app/core/services/rest-api.service";
import { AdminService } from "src/app/core/services/admin.service";
import { map, mergeMap } from "rxjs/operators";
import { from } from "rxjs";
import { Db } from "src/app/core/models/db.model";
import { DataService } from "src/app/core/models/data-service.model";

@Component({
  selector: "app-de-modal",
  templateUrl: "./de-modal.component.html",
  styleUrls: ["./de-modal.component.css"]
})
export class DeModalComponent implements OnInit {
  @Input() data: DataService;
  @Input() mode: string;
  @Input() selectedIndex: number;

  dbs: Array<Db> = [];
  dbTypeIds: Array<string> = [];

  constructor(
    public activeModal: NgbActiveModal,
    public adminService: AdminService,
    private restApiService: RestApiService
  ) {}

  ngOnInit() {
    // Init data
    this.initData();
  }

  initData() {
    this.getDbs();
  }

  getDbs() {
    const get_dbs = this.restApiService.getAllDbs().pipe(
      mergeMap(dbs => from(dbs)),
      map(db => {
        if (!this.dbTypeIds.includes(db.dbTypeId)) {
          this.dbTypeIds.push(db.dbTypeId);
        }
        if (
          this.data.dbId &&
          this.data.dbId.toString().includes(db.id.toString())
        ) {
          db.checkedToSave = true;
        } else {
          db.checkedToSave = false;
        }
        this.dbs.push(db);
      })
    );

    get_dbs.subscribe();
  }

  onClickMatTab(index: number) {
    this.selectedIndex = index;
  }

  isAddingMode() {
    let flag: boolean = false;

    if (this.mode === "new") flag = true;

    return flag;
  }
}
