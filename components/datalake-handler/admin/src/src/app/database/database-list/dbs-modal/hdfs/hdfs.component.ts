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

import { Component, Input, Output, EventEmitter } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { Db } from "src/app/core/models/db.model";
import { AdminService } from "src/app/core/services/admin.service";

@Component({
  selector: "app-hdfs",
  templateUrl: "./hdfs.component.html",
  styleUrls: ["./hdfs.component.css"]
})
export class HdfsComponent {
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  @Input() db: Db;
  tempDb: Db;

  constructor(
    public activeModal: NgbActiveModal,
    public adminService: AdminService
  ) {}

  ngOnInit() {
    // cache for display
    this.tempDb = new Db();
    const feed = {
      name: "HDFS",
      enabled: this.db.enabled,
      host: this.db.host,
      port: this.db.port,
      database: this.db.database,
      encrypt: this.db.encrypt,
      login: this.db.login,
      pass: this.db.pass
    };
    this.tempDb = feed;
  }

  passBack() {
    this.db = this.tempDb;
    this.passEntry.emit(this.db);
  }
}
