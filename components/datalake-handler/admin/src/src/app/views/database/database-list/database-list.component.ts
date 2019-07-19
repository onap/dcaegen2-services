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

import { Component, OnInit, ViewChild, ElementRef } from "@angular/core";
import { Db } from "../../../core/models/db.model";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { DatabaseAddModalComponent } from "./database-add-modal/database-add-modal.component";

// DB modal components
import { RestApiService } from "src/app/core/services/rest-api.service";

// Modal
import { CouchbaseComponent } from "./dbs-modal/couchbase/couchbase.component";
import { DruidComponent } from "./dbs-modal/druid/druid.component";
import { ElasticsearchComponent } from "./dbs-modal/elasticsearch/elasticsearch.component";
import { MongodbComponent } from "./dbs-modal/mongodb/mongodb.component";
import { HdfsComponent } from "./dbs-modal/hdfs/hdfs.component";
import { AlertComponent } from "src/app/shared/components/alert/alert.component";

// Notify
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";

// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";

@Component({
  selector: "app-database-list",
  templateUrl: "./database-list.component.html",
  styleUrls: ["./database-list.component.css"]
})
export class DatabaseListComponent implements OnInit {
  pageFinished: Boolean = false;

  dbList: any = [];
  dbs: Db[] = [];

  loading: Boolean = true;

  tempDbDetail: Db;

  constructor(
    private restApiService: RestApiService,
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
    this.dbList = await this.getDbList();
    setTimeout(() => {
      this.spinner.hide();
    }, 500);
  }

  getDbList() {
    var data: any;

    data = this.restApiService.getDbList().toPromise();

    return data;
  }

  async initDbsList(dbList: []) {
    var d: Db[] = [];

    if (dbList.length > 0) {
      for (var i = 0; i < dbList.length; i++) {
        let data = await this.getDbDetail(dbList[i]);
        let feed = {
          name: dbList[i],
          enabled: data.enabled,
          host: data.host,
          port: data.port,
          database: data.database,
          encrypt: data.encrypt,
          login: data.login,
          pass: data.pass
        };
        d.push(feed);
      }
    }
    return d;
  }

  getDbDetail(name: string) {
    return this.restApiService.getDbDetail(name).toPromise();
  }

  openAddModal() {
    const modalRef = this.modalService.open(DatabaseAddModalComponent, {
      windowClass: "dl-md-modal",
      centered: true
    });

    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      if (receivedEntry) {
        modalRef.close();
        this.openDetailModal(receivedEntry);
      }
    });
  }

  openDetailModal(name: string) {
    var modalRef, index;

    switch (name) {
      case "Couchbase": {
        modalRef = this.modalService.open(CouchbaseComponent, {
          size: "lg",
          centered: true
        });
        break;
      }
      case "Druid": {
        modalRef = this.modalService.open(DruidComponent, {
          size: "lg",
          centered: true
        });
        break;
      }
      case "Elasticsearch": {
        modalRef = this.modalService.open(ElasticsearchComponent, {
          size: "lg",
          centered: true
        });
        break;
      }
      case "MongoDB": {
        modalRef = this.modalService.open(MongodbComponent, {
          size: "lg",
          centered: true
        });
        break;
      }
      case "HDFS": {
        modalRef = this.modalService.open(HdfsComponent, {
          size: "lg",
          centered: true
        });
        break;
      }
      default: {
        break;
      }
    }

    index = this.dbs.findIndex(d => d.name === name);
    this.tempDbDetail = new Db();
    if (index != -1) {
      modalRef.componentInstance.db = this.dbs[index];
    } else {
      modalRef.componentInstance.db = this.tempDbDetail;
    }
    modalRef.componentInstance.passEntry.subscribe(receiveEntry => {
      this.tempDbDetail = receiveEntry;
      let enabled = receiveEntry.enabled;
      console.log(this.tempDbDetail, "this.tempDbDetail");
      if (enabled == true) {
        this.restApiService.upadteDb(this.tempDbDetail).subscribe(
          res => {
            console.log(res);
            if (res.statusCode == 200) {
              this.dbs[index] = this.tempDbDetail;
              this.notificationService.success("SUCCESSFULLY_UPDATED");
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
      } else {
        this.restApiService.upadteDb(this.dbs[index]).subscribe(
          res => {
            console.log(res);
            if (res.statusCode == 200) {
              this.dbs[index] = this.tempDbDetail;
              this.notificationService.success("SUCCESSFULLY_DELETED");
            } else {
              this.dbs[index].encrypt = true;
            }
            modalRef.close();
          },
          err => {
            this.notificationService.error(err);
            modalRef.close();
          }
        );
      }
    });
  }
}
