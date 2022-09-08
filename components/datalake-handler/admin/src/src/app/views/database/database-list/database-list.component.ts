/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2019 - 2020 QCT
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

import { Component, OnInit, ViewChild, ElementRef } from "@angular/core";
import { Db } from "src/app/core/models/db.model";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";

import { RestApiService } from "src/app/core/services/rest-api.service";

// Modal
import { AlertComponent } from "src/app/shared/components/alert/alert.component";
import { ModalComponent } from "src/app/shared/modules/modal/modal.component";
import { ModalContentData } from "src/app/shared/modules/modal/modal.data";
import { DbModalComponent } from "src/app/views/database/database-list/db-modal/db-modal.component";

// Notify
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";

// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";
import { map, mergeMap } from "rxjs/operators";
import { forkJoin, from } from "rxjs";

@Component({
  selector: "app-database-list",
  templateUrl: "./database-list.component.html",
  styleUrls: ["./database-list.component.css"]
})
export class DatabaseListComponent implements OnInit {
  dbs: Array<Db> = []; // data of table
  t_temp: Array<Db> = []; // cache for dbs
  columns: Array<any> = []; // column of table

  @ViewChild("searchText") searchText: ElementRef;

  constructor(
    private restApiService: RestApiService,
    private notificationService: ToastrNotificationService,
    private modalService: NgbModal,
    private spinner: NgxSpinnerService
  ) {}

  ngOnInit() {
    this.spinner.show();

    let t_dbs: Array<Db> = [];

    const get_dbs = this.restApiService.getAllDbs().pipe(
      mergeMap(dbs => from(dbs)),
      map(db => {
        t_dbs.push(db);
      })
    );

    forkJoin(get_dbs).subscribe(data => {
      this.columns = this.initColumn();
      this.dbs = t_dbs;
      this.t_temp = [...this.dbs];
      this.updateFilter(this.searchText.nativeElement.value);
      setTimeout(() => {
        this.spinner.hide();
      }, 500);
    });
  }

  updateFilter(searchValue: string) {
    const val = searchValue.toLowerCase();

    // filter our data
    const temp = this.t_temp.filter(t => {
      return t.name.toLowerCase().indexOf(val) !== -1 || !val;
    });

    // update the rows
    this.dbs = temp;
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
        let newDB: Db = new Db();
        let componentNew = new ModalContentData(DbModalComponent, newDB);

        modalRef.componentInstance.title = "NEW_DB";
        modalRef.componentInstance.notice = "";
        modalRef.componentInstance.mode = "new";
        modalRef.componentInstance.component = componentNew;

        modalRef.componentInstance.passEntry.subscribe((data: Db) => {
          newDB = Object.assign({}, data);
          console.log(newDB.dbTypeId);
          console.log(newDB);
          this.restApiService.addDb(newDB).subscribe(
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
        let index: number = this.dbs.findIndex(db => db.id === id);
        let editDb: Db = this.dbs[index];
        let componentEdit = new ModalContentData(DbModalComponent, editDb);

        modalRef.componentInstance.title = editDb.name;
        modalRef.componentInstance.notice = "";
        modalRef.componentInstance.mode = "edit";
        modalRef.componentInstance.component = componentEdit;

        modalRef.componentInstance.passEntry.subscribe((data: Db) => {
          editDb = Object.assign({}, data);
          this.restApiService.updateDb(editDb).subscribe(
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
