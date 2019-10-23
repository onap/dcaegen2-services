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

import { Component, OnInit, ViewChild, ElementRef } from "@angular/core";
import { Db } from "src/app/core/models/db.model";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { DatabaseAddModalComponent } from "src/app/views/database/database-list/database-add-modal/database-add-modal.component";

// DB modal components
import { RestApiService } from "src/app/core/services/rest-api.service";

// Modal
import { AlertComponent } from "src/app/shared/components/alert/alert.component";

// Notify
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";

// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";
import {CouchbaseComponent} from "src/app/views/database/database-list/dbs-modal/couchbase/couchbase.component";
import {DruidComponent} from "src/app/views/database/database-list/dbs-modal/druid/druid.component";
import {ElasticsearchComponent} from "src/app/views/database/database-list/dbs-modal/elasticsearch/elasticsearch.component";
import {MongodbComponent} from "src/app/views/database/database-list/dbs-modal/mongodb/mongodb.component";
import {HdfsComponent} from "src/app/views/database/database-list/dbs-modal/hdfs/hdfs.component";

@Component({
  selector: "app-database-list",
  templateUrl: "./database-list.component.html",
  styleUrls: ["./database-list.component.css"]
})
export class DatabaseListComponent implements OnInit {
  pageFinished: Boolean = false;

  dbList: any = [];
  dbs: Db[] = [];
  dbNew: Db;
  db_NewBody: Db;
  loading: Boolean = true;
  flag: Boolean = true;
  loadingIndicator: boolean = true;

  mesgNoData = {
    emptyMessage: `
      <div class="d-flex justify-content-center">
        <div class="p-2">
          <label class="dl-nodata">No Data</label>
        </div>
      </div>
    `
  };

  @ViewChild("searchText") searchText: ElementRef;

  constructor(
      private dbApiService: RestApiService,
      private notificationService: ToastrNotificationService,
      private modalService: NgbModal,
      private spinner: NgxSpinnerService
  ) {
    this.initData().then(data => {
      this.initDbsList(this.dbList).then(data => {
        this.dbs = data;
      });
    });
  }

  ngOnInit() {
    this.spinner.show();
  }

  async initData() {
      this.dbList = [];
      this.dbList = await this.getDbList(this.flag);
      setTimeout(() => {
          this.spinner.hide();
      }, 500);
  }

  getDbList(flag) {
    return this.dbApiService.getDbEncryptList(flag).toPromise();

  }

  async initDbsList(dbList: []) {
    var d: Db[] = [];

    for (var i = 0; i < dbList.length; i++) {
      let data = dbList[i];
      let feed = {
        id: data["id"],
        name: data["name"],
        enabled: data["enabled"],
        host: data["host"],
        port: data["port"],
        database: data["database"],
        encrypt: data["encrypt"],
        login: data["login"],
        pass: data["pass"],
        dbTypeId: data["dbTypeId"],
      };
      d.push(feed);
    }
    return d;
  }

  // getDbDetail(name: string) {
  //   return this.restApiService.getDbDetail(name).toPromise();
  // }

  openAddModal() {
    const modalRef = this.modalService.open(DatabaseAddModalComponent, {
      windowClass: "dl-md-modal",
      centered: true
    });

    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      if (receivedEntry) {
        modalRef.close();
        //this.openDetailModal(receivedEntry);
      }
    });
  }

  updateFilter(searchValue) {
    const val = searchValue.toLowerCase();
    // filter our data
    const temps = this.dbList.filter(function (d) {
      return d.name.toLowerCase().indexOf(val) != -1 || !val;
    });
    // update the rows
    this.dbList = temps;
  }

  newDbModal() {
    const modalRef = this.modalService.open(DatabaseAddModalComponent, {
      windowClass: "dl-md-modal dbs",
      centered: true
    });
  }

  deleteDbModel(id: number) {

    console.log("delete id", id);
    const index = this.dbList.findIndex(t => t.id === id);
    const modalRef = this.modalService.open(AlertComponent, {
      size: "sm",
      centered: true
    });
    modalRef.componentInstance.message = "ARE_YOU_SURE_DELETE";
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      // Delete db
      this.dbApiService.deleteDb(id).subscribe(
        res => {
          console.log(res);
          if (JSON.stringify(res).length <= 2) {
            this.dbList.splice(index, 1);
            this.dbList = [...this.dbList];
            this.initData();
            this.notificationService.success("SUCCESSFULLY_DELETED");

          } else {
            this.initData();
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

  updateDbModel(id: number, dbType: string) {
    var modalRef;
    console.log(dbType, "dbType");
    switch (dbType) {
      case "CB": {
        modalRef = this.modalService.open(CouchbaseComponent, {
          size: "lg",
          centered: true
        });
        this.editDbModal(id, modalRef);
        break;
      }
      case "DRUID": {
        modalRef = this.modalService.open(DruidComponent, {
          size: "lg",
          centered: true
        });
        this.editDbModal(id, modalRef);
        break;
      }
      case "ES": {
        modalRef = this.modalService.open(ElasticsearchComponent, {
          size: "lg",
          centered: true
        });
        this.editDbModal(id, modalRef);
        break;
      }
      case "MONGO": {
        modalRef = this.modalService.open(MongodbComponent, {
          size: "lg",
          centered: true
        });
        this.editDbModal(id, modalRef);
        break;
      }
      case "HDFS": {
        modalRef = this.modalService.open(HdfsComponent, {
          size: "lg",
          centered: true
        });
        this.editDbModal(id, modalRef);
        break;
      }
      default: {
        break;
      }
    }
  }

  editDbModal(id: number, modalRef) {
    console.log("id", id);
    const index = this.dbList.findIndex(t => t.id === id);
    modalRef.componentInstance.editDb = this.dbList[index];
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      this.dbNew = receivedEntry;
      this.dbApiService
        .updateDb(this.dbNew)
        .subscribe(
          res => {
            if (res.statusCode == 200) {
              this.dbList[index] = this.dbNew;
              this.dbList = [...this.dbList];
              this.notificationService.success("SUCCESSFULLY_UPDATED");
              this.initData();
            } else {
              this.notificationService.error("FAILED_UPDATED");
              this.initData();
            }
            modalRef.close();
          },
          err => {
            this.notificationService.error(err);
            modalRef.close();
          }
        );
    })
  }

  onActivate(event) {

  }
}
