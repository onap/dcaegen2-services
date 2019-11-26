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
 *
 */

import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {Db} from "src/app/core/models/db.model";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {AdminService} from "src/app/core/services/admin.service";
import {NgxSpinnerService} from "ngx-spinner";

@Component({
  selector: 'app-modal-tools',
  templateUrl: './modal-tools.component.html',
  styleUrls: ['./modal-tools.component.css']
})
export class ModalToolsComponent implements OnInit {

  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  @Input() editTool: Db;
  @Input() tool: Db;
  @Input() toolList_length;
  toolInput: Db;
  @ViewChild("d_dbTypeId") d_dbTypeId: ElementRef;
  dbTypeIdList: Array<string> = [];
  defaultDbType: string;
  toolInputTitle = "";
  data: string;
  constructor(
    public activeModal: NgbActiveModal,
    public adminService: AdminService,
    private spinner: NgxSpinnerService
  ) { }

  ngOnInit() {
    if (this.editTool == null) {
      this.toolInput = new Db();
      const feed = {
        id: null,
        name: this.tool.name,
        enabled: this.tool.enabled,
        host: this.tool.host,
        port: this.tool.port,
        database: this.tool.database,
        encrypt: this.tool.encrypt,
        login: this.tool.login,
        pass: this.tool.pass,
        dbTypeId: this.tool.dbTypeId
      }
      this.toolInput = feed;
      this.toolInputTitle = this.data === "Kibana" ? "New Kibana" : "New Superset";
      this.dbTypeIdList = this.data === "Kibana" ? ["KIBANA"] : ["SUPERSET"];
      console.log("create db");

    } else {
      this.toolInput = this.editTool;
      this.toolInputTitle = "Edit" + "-" + this.editTool.dbTypeId + "-" + this.editTool.name;
      this.defaultDbType = this.editTool.dbTypeId;
      this.dbTypeIdList = [this.editTool.dbTypeId];
      console.log("edit db");
    }
  }

  passBack() {
    this.spinner.show();
    if (this.toolInput.name == '' || this.toolInput.name == undefined) {
      return false;
    }
    this.editTool = this.toolInput;
    this.editTool.dbTypeId = this.d_dbTypeId.nativeElement.value;
    this.editTool.encrypt = false;
    console.log(this.editTool, "editTool");
    this.passEntry.emit(this.editTool);
    setTimeout(() => {
      this.spinner.hide();
    }, 500);
  }

}
