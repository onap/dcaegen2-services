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
 */

import { Component, ViewChild, ElementRef } from "@angular/core";
import { RestApiService } from "src/app/core/services/rest-api.service";
import { Topic } from "src/app/core/models/topic.model";
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
import { TopicModalComponent } from "src/app/views/topics/topic-list/topic-modal/topic-modal.component";

@Component({
  selector: "app-topic-list",
  templateUrl: "./topic-list.component.html",
  styleUrls: ["./topic-list.component.css"]
})
export class TopicListComponent {
  topics: Array<Topic> = []; // data of table
  t_temp: Array<Topic> = []; // cache for topics
  t_default: Topic = new Topic();

  columns: Array<any> = []; // column of table

  @ViewChild("searchText") searchText: ElementRef;

  //TODO
  //tempTopicDetail: Topic; // temp for a topic
  //tempNewTopic: Topic; // temp for a newtopic

  constructor(
    private restApiService: RestApiService,
    private modalService: NgbModal,
    private notificationService: ToastrNotificationService,
    private spinner: NgxSpinnerService
  ) {}

  ngOnInit() {
    this.spinner.show();
    let t_feeder: Array<Topic> = [];

    const get_t_feeder = this.restApiService.getTopicList().pipe(
      mergeMap(ids => from(ids)),
      mergeMap(id => this.restApiService.getTopic(id)),
      map(t => {
        t.config = true;
        t.countsDb.MONGO > 0
          ? (t.countsMONGO = t.countsDb.MONGO)
          : (t.countsMONGO = 0);
        t.countsDb.DRUID > 0
          ? (t.countsDRUID = t.countsDb.DRUID)
          : (t.countsDRUID = 0);
        t.countsDb.HDFS > 0
          ? (t.countsHDFS = t.countsDb.HDFS)
          : (t.countsHDFS = 0);
        t.countsDb.ES > 0 ? (t.countsES = t.countsDb.ES) : (t.countsES = 0);
        t.countsDb.CB > 0 ? (t.countsCB = t.countsDb.CB) : (t.countsCB = 0);
        t_feeder.push(t);
      })
    );

    const get_t_default = this.restApiService.getTopicDefault();

    forkJoin(get_t_feeder, get_t_default).subscribe(data => {
      this.columns = this.initColumn();
      this.t_default = data[1];
      this.topics = t_feeder;
      this.t_temp = [...this.topics];
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
        headerName: "ID",
        width: "15",
        sortable: true,
        dataIndex: "id"
      },
      {
        headerName: "NAME",
        width: "420",
        sortable: true,
        dataIndex: "name"
      },
      {
        headerIcon: "assets/icons/kafka_able.svg",
        headerIconInfo: "Kafka",
        width: "10",
        sortable: true,
        dataIndex: "countsKafka"
      },
      {
        headerIcon: "assets/icons/couchbase_able.svg",
        headerIconInfo: "Couchbase",
        width: "10",
        sortable: true,
        dataIndex: "countsCB"
      },
      {
        headerIcon: "assets/icons/druid_able.svg",
        headerIconInfo: "Druid",
        width: "10",
        sortable: true,
        dataIndex: "countsDRUID"
      },
      {
        headerIcon: "assets/icons/elasticsearch_able.svg",
        headerIconInfo: "Elasticsearch",
        width: "10",
        sortable: true,
        dataIndex: "countsES"
      },
      {
        headerIcon: "assets/icons/mongoDB_able.svg",
        headerIconInfo: "MongoDB",
        width: "10",
        sortable: true,
        dataIndex: "countsMONGO"
      },
      {
        headerIcon: "assets/icons/hadoop_able.svg",
        headerIconInfo: "Hadoop",
        width: "10",
        sortable: true,
        dataIndex: "countsHDFS"
      },
      {
        headerName: "TTL",
        width: "20",
        sortable: true,
        dataIndex: "ttl"
      },
      {
        headerName: "SAVE_RAW_DATA",
        width: "20",
        sortable: true,
        dataIndex: "saveRaw",
        icon: "check"
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
          this.restApiService.deleteTopic(id).subscribe(
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

  openModal(mode: string = "", t_id: number | string) {
    const modalRef = this.modalService.open(ModalComponent, {
      size: "lg",
      centered: true,
      backdrop: "static"
    });

    switch (mode) {
      case "new":
        // Open new modal for topic
        let newTopic: Topic;
        newTopic = Object.assign({}, this.t_default);
        newTopic.id = null;
        newTopic.name = "";
        let componentNew = new ModalContentData(TopicModalComponent, newTopic);

        modalRef.componentInstance.title = "NEW_TOPIC";
        modalRef.componentInstance.notice = "TOPIC_NEW_NOTICE";
        modalRef.componentInstance.mode = "new";
        modalRef.componentInstance.component = componentNew;

        modalRef.componentInstance.passEntry.subscribe((data: Topic) => {
          newTopic = Object.assign({}, data);
          this.restApiService.addTopic(newTopic).subscribe(
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
        // Open edit modal for topic
        let index: number = this.topics.findIndex(t => t.id === t_id);
        let editTopic: Topic = this.topics[index];
        let componentEdit = new ModalContentData(
          TopicModalComponent,
          editTopic
        );

        modalRef.componentInstance.title = editTopic.name;
        modalRef.componentInstance.notice = "";
        modalRef.componentInstance.mode = "edit";
        modalRef.componentInstance.component = componentEdit;

        modalRef.componentInstance.passEntry.subscribe((data: Topic) => {
          editTopic = Object.assign({}, data);
          this.restApiService.updateTopic(editTopic).subscribe(
            res => {
              // this.topics[index] = editTopic;
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
      case "default":
        // Open default config modal for topic
        let componentDefault = new ModalContentData(
          TopicModalComponent,
          this.t_default
        );

        modalRef.componentInstance.title = "DEFAULT_CONFIGURATIONS";
        modalRef.componentInstance.notice = "TOPIC_DEFAULT_CONF_NOTICE";
        modalRef.componentInstance.mode = "edit";
        modalRef.componentInstance.component = componentDefault;

        modalRef.componentInstance.passEntry.subscribe((data: Topic) => {
          this.t_default = Object.assign({}, data);
          this.restApiService.updateTopic(this.t_default).subscribe(
            res => {
              this.notificationService.success("SUCCESSFULLY_UPDATED");
            },
            err => {
              this.notificationService.error("FAILED_UPDATED");
            }
          );
          modalRef.close();
        });
        break;
    }
  }

  updateFilter(searchValue: string) {
    const val = searchValue.toLowerCase();

    // filter our data
    const temp = this.t_temp.filter(t => {
      return t.name.toLowerCase().indexOf(val) !== -1 || !val;
    });

    // update the rows
    this.topics = temp;
  }
}
