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

import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {Db} from "src/app/core/models/db.model";
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {NgxSpinnerService} from "ngx-spinner";
import {ToastrNotificationService} from "src/app/shared/components/toastr-notification/toastr-notification.service";
import {RestApiService} from "src/app/core/services/rest-api.service";
import {ModalToolsComponent} from "src/app/views/tools/modal-tools/modal-tools.component";

@Component({
  selector: 'app-tool-add-modal',
  templateUrl: './tool-add-modal.component.html',
  styleUrls: ['./tool-add-modal.component.css']
})
export class ToolAddModalComponent implements OnInit {

  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  seletedItem: string = "";
  toolList: any = [];
  loading: Boolean = true;
  toolNew: Db;
  tool_NewBody: Db;
  constructor(
    public activeModal: NgbActiveModal,
    private spinner: NgxSpinnerService,
    private notificationService: ToastrNotificationService,
    private modalService: NgbModal,
    private dbApiService: RestApiService
  ) { }

  ngOnInit() {
  }

  clickItem(name: string) {
    this.seletedItem = name;
  }

  passBack() {
    console.log(this.seletedItem, "next");
    this.openNewModal(this.seletedItem);
  }

  newTool(modalRef) {
    this.toolNew = new Db();
    this.tool_NewBody = new Db();
    modalRef.componentInstance.tool = this.tool_NewBody;
    modalRef.componentInstance.toolList_length = this.toolList.length;
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      this.tool_NewBody = receivedEntry;
      this.dbApiService
        .createDb(this.tool_NewBody)
        .subscribe(
          res => {
            this.spinner.hide();
            if (res.statusCode == 200) {
              this.toolNew = res.returnBody;
              this.toolList.push(this.toolNew);
              this.toolList = [...this.toolList];
              this.notificationService.success("SUCCESSFULLY_CREARED");
            } else {
              this.notificationService.error("FAILED_CREARED");
            }
            modalRef.close();
          },
          err => {
            this.spinner.hide();
            this.notificationService.error(err);
            modalRef.close();
          }
        );
    });
  }

  openNewModal(name: string) {
    let modalRef;

    switch (name) {
      case "Kibana": {
        modalRef = this.modalService.open(ModalToolsComponent, {
          size: "lg",
          centered: true
        });
        modalRef.componentInstance.data = name;
        this.newTool(modalRef);
        break;
      }
      case "Superset": {
        modalRef = this.modalService.open(ModalToolsComponent, {
          size: "lg",
          centered: true
        });
        modalRef.componentInstance.data = name;
        this.newTool(modalRef);
        break;
      }
      default: {
        break;
      }
    }
  }

}
