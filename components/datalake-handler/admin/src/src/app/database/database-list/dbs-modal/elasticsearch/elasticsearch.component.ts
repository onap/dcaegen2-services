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

@Component({
  selector: "app-elasticsearch",
  templateUrl: "./elasticsearch.component.html",
  styleUrls: ["./elasticsearch.component.css"]
})
export class ElasticsearchComponent {
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  @Input() db: Db;
  tempDb: Db;

  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit() {
    // cache for display
    this.tempDb = new Db();
    const feed = {
      name: "Elasticsearch",
      enabled: true, // TODO: enable
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

    console.log("==================================");
    console.log("Update db name: " + this.db.name);
    console.log("Update db enabled: " + this.db.enabled);
    console.log("Update db host: " + this.db.host);
    console.log("Update db port: " + this.db.port);
    console.log("Update db database: " + this.db.database);
    console.log("Update db encrypt: " + this.db.encrypt);
    console.log("Update db login: " + this.db.login);
    console.log("Update db pass: " + this.db.pass);
    console.log("==================================");

    this.passEntry.emit(this.db);
  }
}
