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

import { Component, ViewChild, ElementRef, OnInit } from "@angular/core";
import { RestApiService } from "src/app/core/services/rest-api.service";
import { AdminService } from "src/app/core/services/admin.service";
import { DataService } from "src/app/core/models/data-service.model";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";

// Notify
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";

// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";

import { AlertComponent } from "src/app/shared/components/alert/alert.component";
import { map, mergeMap } from "rxjs/operators";
import { forkJoin, from } from "rxjs";

// Modal
import { ModalComponent } from "src/app/shared/modules/modal/modal.component";
import { ModalContentData } from "src/app/shared/modules/modal/modal.data";
import { DeModalComponent } from "src/app/views/data-exposure/de-modal/de-modal.component";

@Component({
  selector: "app-data-exposure",
  templateUrl: "./data-exposure.component.html",
  styleUrls: ["./data-exposure.component.css"]
})
export class DataExposureComponent implements OnInit {
  datas: Array<DataService> = [];
  t_temp: Array<DataService> = [];
  columns: Array<any> = []; // column of table

  @ViewChild("searchText") searchText: ElementRef;
  constructor(
    private restApiService: RestApiService,
    private modalService: NgbModal,
    private notificationService: ToastrNotificationService,
    private spinner: NgxSpinnerService,
    private adminService: AdminService
  ) {
    // Set page title
    this.adminService.setTitle("SIDEBAR.DATAEXPOSURE");
  }

  ngOnInit() {
    this.spinner.show();
    let t_data: Array<DataService> = [];

    const get_ds = this.restApiService.getAllDataService().pipe(
      mergeMap(dss => from(dss)),
      map(data => {
        t_data.push(data);
      })
    );

    forkJoin(get_ds).subscribe(data => {
      this.columns = this.initColumn();
      this.datas = t_data;
      this.t_temp = [...this.datas];
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
        headerName: "NAME",
        width: "420",
        sortable: true,
        dataIndex: "id"
      },
      {
        headerName: "DESCRIPTION",
        width: "420",
        sortable: true,
        dataIndex: "note"
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
    const temp = this.t_temp.filter(data => {
      return data.id.toLowerCase().indexOf(val) !== -1 || !val;
    });

    // update the rows
    this.datas = temp;
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
          this.restApiService.deleteDataService(id).subscribe(
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
        let newDS: DataService;
        let componentNew = new ModalContentData(DeModalComponent, newDS);

        modalRef.componentInstance.title = "NEW_DATASERVICE";
        modalRef.componentInstance.notice = "";
        modalRef.componentInstance.mode = "new";
        modalRef.componentInstance.component = componentNew;

        modalRef.componentInstance.passEntry.subscribe((data: DataService) => {
          newDS = Object.assign({}, data);
          this.restApiService.addDataService(newDS).subscribe(
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
        let index: number = this.datas.findIndex(data => data.id === id);
        let editDS: DataService = this.datas[index];
        let componentEdit = new ModalContentData(DeModalComponent, editDS);

        modalRef.componentInstance.title = editDS.id;
        modalRef.componentInstance.notice = "";
        modalRef.componentInstance.mode = "edit";
        modalRef.componentInstance.component = componentEdit;

        modalRef.componentInstance.passEntry.subscribe((data: DataService) => {
          editDS = Object.assign({}, data);
          this.restApiService.updateDataService(editDS).subscribe(
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
