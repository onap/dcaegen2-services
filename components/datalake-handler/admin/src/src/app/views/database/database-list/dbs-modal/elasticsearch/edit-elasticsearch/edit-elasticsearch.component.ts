/*
    Copyright (C) 2019 CMCC, Inc. and others. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
/**
 *
 * @author Chunmeng Guo
 */

import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {Db} from "src/app/core/models/db.model";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {NgxSpinnerService} from "ngx-spinner";

@Component({
  selector: 'app-edit-elasticsearch',
  templateUrl: './edit-elasticsearch.component.html',
  styleUrls: ['./edit-elasticsearch.component.css']
})
export class EditElasticsearchComponent implements OnInit {

  @Input() editDb: Db;
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  dbInput: Db;
  dbTypeIdList: Array<string> = ["CB", "DRUID", "ES", "HDFS", "MONGO"];
  defaultDbType: string;
  @ViewChild("d_dbTypeId") d_dbTypeId: ElementRef;
  constructor(
    public activeModal: NgbActiveModal,
    private spinner: NgxSpinnerService,
  ) { }

  dbInputTitle = "";

  ngOnInit() {
    this.dbInput = this.editDb;
    this.dbInputTitle = this.editDb.name;
    this.defaultDbType = this.dbInput.dbTypeId;
  }

  passBack() {
    this.spinner.show();
    if (this.dbInput.name == '' || this.dbInput.name == undefined) {
      return false;
    }
    this.editDb = this.dbInput;
    this.editDb.dbTypeId = this.d_dbTypeId.nativeElement.value;
    console.log(this.editDb, "uodateDb")
    this.passEntry.emit(this.editDb);
    setTimeout(() => {
      this.spinner.hide();
    }, 500);
  }

}
