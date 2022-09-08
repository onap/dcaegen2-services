/*
    Copyright (C) 2019 - 2020 CMCC, Inc. and others. All rights reserved.

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
 * @constructor Ekko Chang
 */

import { Component, ElementRef, OnInit, ViewChild } from "@angular/core";
import { AdminService } from "src/app/core/services/admin.service";
import { Db } from "src/app/core/models/db.model";
import { RestApiService } from "src/app/core/services/rest-api.service";

// Modal
import { AlertComponent } from "src/app/shared/components/alert/alert.component";
import { ModalComponent } from "src/app/shared/modules/modal/modal.component";
import { ModalContentData } from "src/app/shared/modules/modal/modal.data";
import { ToolModalComponent } from "src/app/views/tools/tool-modal/tool-modal.component";
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";

import { NgxSpinnerService } from "ngx-spinner";
import { map, mergeMap } from "rxjs/operators";
import { forkJoin, from } from "rxjs";

@Component({
  selector: "app-tools",
  templateUrl: "./tools.component.html",
  styleUrls: ["./tools.component.css"]
})
export class ToolsComponent implements OnInit {
  columns: Array<any> = [];
  tools: Array<any> = [];
  t_temp: Array<any> = []; // cache for tools

  @ViewChild("searchText") searchText: ElementRef;

  constructor(
    private adminService: AdminService,
    private notificationService: ToastrNotificationService,
    private modalService: NgbModal,
    private restApiService: RestApiService,
    private spinner: NgxSpinnerService
  ) {
    this.adminService.setTitle("SIDEBAR.DASHBOARDLIST");
    // this.initData().then(data => {});
  }

  ngOnInit() {
    this.spinner.show();

    let t_tools: Array<Db> = [];

    const get_tools = this.restApiService.getAllTools().pipe(
      mergeMap(tools => from(tools)),
      map(tool => {
        t_tools.push(tool);
      })
    );

    forkJoin(get_tools).subscribe(data => {
      this.columns = this.initColumn();
      this.tools = t_tools;
      this.t_temp = [...this.tools];
      this.updateFilter(this.searchText.nativeElement.value);
      setTimeout(() => {
        this.spinner.hide();
      }, 500);
    });
  }

  initColumn() {
    let t_columns: Array<any> = [];

    t_columns = [
      {
        headerName: "STATUS",
        width: "15",
        sortable: true,
        dataIndex: "enabled",
        icon: "status"
      },
      {
        headerName: "NAME",
        width: "420",
        sortable: true,
        dataIndex: "name"
      },
      {
        headerName: "Type",
        width: "50",
        sortable: true,
        dataIndex: "dbTypeId"
      },
      {
        headerName: "Host",
        width: "100",
        sortable: true,
        dataIndex: "host"
      },
      {
        width: "2",
        iconButton: "cog",
        action: "edit"
      },
      {
        width: "2",
        iconButton: "trash",
        action: "delete"
      }
    ];

    return t_columns;
  }

  updateFilter(searchValue: string) {
    const val = searchValue.toLowerCase();

    // filter our data
    const temp = this.t_temp.filter(t => {
      return t.name.toLowerCase().indexOf(val) !== -1 || !val;
    });

    // update the rows
    this.tools = temp;
  }

  btnTableAction(passValueArr: Array<any>) {
    let action = passValueArr[0];
    let id = passValueArr[1];

    switch (action) {
      case "edit":
        this.openModal("edit", id);
        break;
      case "delete":
        const modalRef = this.modalService.open(AlertComponent, {
          size: "sm",
          centered: true,
          backdrop: "static"
        });
        modalRef.componentInstance.message = "ARE_YOU_SURE_DELETE";
        modalRef.componentInstance.passEntry.subscribe(recevicedEntry => {
          this.restApiService.deleteDb(id).subscribe(
            res => {
              this.ngOnInit();
              setTimeout(() => {
                this.notificationService.success("SUCCESSFULLY_DELETED");
              }, 500);
            },
            err => {
              this.notificationService.error(err);
            }
          );
          modalRef.close();
        });
        break;
    }
  }

  openModal(mode: string = "", id: number | string) {
    const modalRef = this.modalService.open(ModalComponent, {
      size: "lg",
      centered: true,
      backdrop: "static"
    });

    switch (mode) {
      case "new":
        let newTool: Db = new Db();
        let componentNew = new ModalContentData(ToolModalComponent, newTool);

        modalRef.componentInstance.title = "NEW_TOOL";
        modalRef.componentInstance.notice = "";
        modalRef.componentInstance.mode = "new";
        modalRef.componentInstance.component = componentNew;

        modalRef.componentInstance.passEntry.subscribe((data: Db) => {
          newTool = Object.assign({}, data);
          console.log(newTool.dbTypeId);
          console.log(newTool);
          this.restApiService.addDb(newTool).subscribe(
            res => {
              this.ngOnInit();
              setTimeout(() => {
                this.notificationService.success("SUCCESSFULLY_CREARED");
              }, 500);
            },
            err => {
              this.notificationService.error(err);
            }
          );
          modalRef.close();
        });
        break;
      case "edit":
        let index: number = this.tools.findIndex(db => db.id === id);
        let editTool: Db = this.tools[index];
        let componentEdit = new ModalContentData(ToolModalComponent, editTool);

        modalRef.componentInstance.title = editTool.name;
        modalRef.componentInstance.notice = "";
        modalRef.componentInstance.mode = "edit";
        modalRef.componentInstance.component = componentEdit;

        modalRef.componentInstance.passEntry.subscribe((data: Db) => {
          editTool = Object.assign({}, data);
          this.restApiService.updateDb(editTool).subscribe(
            res => {
              this.ngOnInit();
              setTimeout(() => {
                this.notificationService.success("SUCCESSFULLY_UPDATED");
              }, 500);
            },
            err => {
              this.notificationService.error(err);
            }
          );
          modalRef.close();
        });
        break;
    }
  }
}
