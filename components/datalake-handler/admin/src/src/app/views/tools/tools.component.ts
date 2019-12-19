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

import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {AdminService} from "src/app/core/services/admin.service";
import {Db} from "src/app/core/models/db.model";
import {RestApiService} from "src/app/core/services/rest-api.service";
import {AlertComponent} from "src/app/shared/components/alert/alert.component";
import {ToastrNotificationService} from "src/app/shared/components/toastr-notification/toastr-notification.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ToolAddModalComponent} from "src/app/views/tools/tool-add-modal/tool-add-modal.component";
import {ModalToolsComponent} from "src/app/views/tools/modal-tools/modal-tools.component";

@Component({
  selector: 'app-tools',
  templateUrl: './tools.component.html',
  styleUrls: ['./tools.component.css']
})
export class ToolsComponent implements OnInit {

  toolsColumns: Array<any> = [];
  toolsList: Array<any> = [];
  dbs: Db[] = [];
  toolNew: Db;
  loading: Boolean = true;
  flag: Boolean = false;

  @ViewChild("searchText") searchText: ElementRef;

  constructor(
    private adminService: AdminService,
    private notificationService: ToastrNotificationService,
    private modalService: NgbModal,
    private dbApiService: RestApiService
  ) {
    this.adminService.setTitle("SIDEBAR.DASHBOARDLIST");
    this.initData().then(data => { });
  }

  ngOnInit() {
    this.toolsColumns = [
      {
        name: "STATUS",
        width: "50",
        dataIndex: "enabled"
      },
      {
        name: "NAME",
        width: "220",
        dataIndex: "name"
      },
      {
        name: "DB_TYPE",
        width: "220",
        dataIndex: "dbTypeId"
      },
      {
        name: "",
        width: "5",
        dataIndex: "",
        icon: "trash"
      },
      {
        name: "",
        width: "5",
        dataIndex: "",
        icon: "cog"
      }
    ];
  }

  async initData() {
    this.toolsList = await this.dbApiService.getDbEncryptList(this.flag).toPromise();
  }

  updateFilter(searchValue) {
    const val = searchValue.toLowerCase();
    // filter our data
    const temps = this.toolsList.filter(function (d) {
      return d.name.toLowerCase().indexOf(val) != -1 || !val;
    });
    // update the rows
    this.toolsList = temps;
  }

  openModalDemo() {
    this.modalService.open(ToolAddModalComponent, {
      windowClass: "dl-md-modal dbs",
      size: "sm",
      centered: true
    });
  }

  dataAction($event) {
    if($event[0] == "trash"){
      console.log($event, "tools delete");
      this.deleteToolModel($event[1]);
    }else {
      console.log($event, "tools update");
      this.updateToolModel($event[1]);
    }
  }

  deleteToolModel(id: number) {

    const modalRef = this.modalService.open(AlertComponent, {
      size: "sm",
      centered: true
    });
    modalRef.componentInstance.message = "ARE_YOU_SURE_DELETE";
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      // Delete tool
      this.dbApiService.deleteDb(id).subscribe(
        res => {
          if (JSON.stringify(res).length <= 2) {
            this.toolsList = [...this.toolsList];
            this.notificationService.success("SUCCESSFULLY_DELETED");
            this.initData();
          } else {
            this.notificationService.error("FAILED_DELETED");
          }
          modalRef.close();
        },
        err => {
          this.notificationService.error(err);
          modalRef.close();
        }
      );
    });
  }

  updateToolModel(id: number) {
    const modalRef = this.modalService.open(ModalToolsComponent, {
      size: "lg",
      centered: true
    });
    const index = this.toolsList.findIndex(t => t.id === id);
    modalRef.componentInstance.editTool = this.toolsList[index];
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      this.toolNew = receivedEntry;
      this.dbApiService
        .updateDb(this.toolNew)
        .subscribe(
          res => {
            if (res.statusCode == 200) {
              this.toolsList[index] = this.toolNew;
              this.toolsList = [...this.toolsList];
              this.notificationService.success("SUCCESSFULLY_UPDATED");
              this.initData();
            } else {
              this.notificationService.error("FAILED_UPDATED");
            }
            modalRef.close();
          },
          err => {
            this.notificationService.error(err);
            modalRef.close();
          }
        );
    });
  }
}
