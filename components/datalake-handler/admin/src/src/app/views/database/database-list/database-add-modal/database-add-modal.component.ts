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

import {Component, Output, EventEmitter, ViewChild, ElementRef} from "@angular/core";
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CouchbaseComponent} from "src/app/views/database/database-list/dbs-modal/couchbase/couchbase.component";
import {DruidComponent} from "src/app/views/database/database-list/dbs-modal/druid/druid.component";
import {ElasticsearchComponent} from "src/app/views/database/database-list/dbs-modal/elasticsearch/elasticsearch.component";
import {MongodbComponent} from "src/app/views/database/database-list/dbs-modal/mongodb/mongodb.component";
import {HdfsComponent} from "src/app/views/database/database-list/dbs-modal/hdfs/hdfs.component";
import {Db} from "src/app/core/models/db.model";
import {RestApiService} from "src/app/core/services/rest-api.service";
import {ToastrNotificationService} from "src/app/shared/components/toastr-notification/toastr-notification.service";
import {NgxSpinnerService} from "ngx-spinner";

@Component({
  selector: "app-database-add-modal",
  templateUrl: "./database-add-modal.component.html",
  styleUrls: ["./database-add-modal.component.css"]
})
export class DatabaseAddModalComponent {
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  seletedItem: string = "";
  dbList: any = [];
  dbs: Db[] = [];
  loading: Boolean = true;
  dbNew: Db;
  db_NewBody: Db;
  constructor(
    public activeModal: NgbActiveModal,
    private spinner: NgxSpinnerService,
    private notificationService: ToastrNotificationService,
    private modalService: NgbModal,
    private dbApiService: RestApiService
  ) {}

  ngOnInit() {}

  clickItem(name: string) {
    this.seletedItem = name;
  }

  passBack() {
    console.log(this.seletedItem, "next");
    this.openNewModal(this.seletedItem);
  }

  newDb(modalRef) {
    this.dbNew = new Db();
    this.db_NewBody = new Db();
    modalRef.componentInstance.db = this.db_NewBody;
    modalRef.componentInstance.dbList_length = this.dbList.length;
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      this.db_NewBody = receivedEntry;
      this.dbApiService
        .createDb(this.db_NewBody)
        .subscribe(
          res => {
            this.spinner.hide();
            if (res.statusCode == 200) {
              this.dbNew = res.returnBody;
              this.dbList.push(this.dbNew);
              this.dbList = [...this.dbList];
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
    var modalRef, index;

    switch (name) {
      case "Couchbase": {
        modalRef = this.modalService.open(CouchbaseComponent, {
          size: "lg",
          centered: true
        });
        this.newDb(modalRef);
        break;
      }
      case "Druid": {
        modalRef = this.modalService.open(DruidComponent, {
          size: "lg",
          centered: true
        });
        this.newDb(modalRef);
        break;
      }
      case "Elasticsearch": {
        modalRef = this.modalService.open(ElasticsearchComponent, {
          size: "lg",
          centered: true
        });
        this.newDb(modalRef);
        break;
      }
      case "MongoDB": {
        modalRef = this.modalService.open(MongodbComponent, {
          size: "lg",
          centered: true
        });
        this.newDb(modalRef);
        break;
      }
      case "HDFS": {
        modalRef = this.modalService.open(HdfsComponent, {
          size: "lg",
          centered: true
        });
        this.newDb(modalRef);
        break;
      }
      default: {
        break;
      }
    }
  }
}
