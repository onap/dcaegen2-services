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
 * @contributor Chunmeng Guo
 *
 */

import {Component, Input, Output, EventEmitter, ViewChild, ElementRef} from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { Db } from "src/app/core/models/db.model";
import { AdminService } from "src/app/core/services/admin.service";
import {NgxSpinnerService} from "ngx-spinner";

@Component({
  selector: "app-mongodb",
  templateUrl: "./mongodb.component.html",
  styleUrls: ["./mongodb.component.css"]
})
export class MongodbComponent {
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  @Input() editDb: Db;
  @Input() db: Db;
  @Input() dbList_length;
  dbInput: Db;
  dbTypeIdList: Array<string> = ["CB", "DRUID", "ES", "HDFS", "MONGO"];
  @ViewChild("d_dbTypeId") d_dbTypeId: ElementRef;

  defaultDbType: string;
  dbInputTitle = "";
  constructor(
    public activeModal: NgbActiveModal,
    public adminService: AdminService,
    private spinner: NgxSpinnerService
  ) { }

  ngOnInit() {
    if (this.editDb == null) {
      this.dbInput = new Db();
      const feed = {
        id: null,
        name: this.db.name,
        enabled: this.db.enabled,
        host: this.db.host,
        port: this.db.port,
        database: this.db.database,
        encrypt: this.db.encrypt,
        login: this.db.login,
        pass: this.db.pass,
        dbTypeId: this.db.dbTypeId
      }
      this.dbInput = feed;
      this.dbInputTitle = "New MongoDb";
      console.log("create db");

    } else {
      this.dbInput = this.editDb;
      this.dbInputTitle = "Edit " + this.editDb.name;
      this.defaultDbType = this.dbInput.dbTypeId;
      console.log("edit db");
    }
  }

  passBack() {
    this.spinner.show();
    if (this.dbInput.name == '' || this.dbInput.name == undefined) {
      return false;
    }
    this.editDb = this.dbInput;
    this.editDb.dbTypeId = this.d_dbTypeId.nativeElement.value;
    console.log(this.editDb, "db");
    this.passEntry.emit(this.editDb);
    setTimeout(() => {
      this.spinner.hide();
    }, 500);
  }
}
