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

import { Component, ViewChild, ElementRef } from "@angular/core";
import { RestApiService } from "src/app/core/services/rest-api.service";
import { Topic } from "src/app/core/models/topic.model";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";

// modal
import { TopicDetailModalComponent } from "./topic-detail-modal/topic-detail-modal.component";
import { TopicConfigModalComponent } from "./topic-config-modal/topic-config-modal.component";
import { NewTopicModelComponent } from "./new-topic-model/new-topic-model.component";

// notify
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";

// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";

import { AlertComponent } from "src/app/shared/components/alert/alert.component";
import { map, mergeMap } from "rxjs/operators";
import { forkJoin, from } from "rxjs";
import { HttpClient } from "@angular/common/http";

@Component({
  selector: "app-topic-list",
  templateUrl: "./topic-list.component.html",
  styleUrls: ["./topic-list.component.css"]
})
export class TopicListComponent {
  topics: Array<Topic> = []; // data of table
  columns: Array<any> = []; // column of table
  t_temp: Array<Topic> = []; // cache for topics

  //TODO
  //tempTopicDetail: Topic; // temp for a topic
  //tempNewTopic: Topic; // temp for a newtopic

  constructor(
    private restApiService: RestApiService,
    private modalService: NgbModal,
    private notificationService: ToastrNotificationService,
    private spinner: NgxSpinnerService,
    public http: HttpClient
  ) {}

  ngOnInit() {
    //this.spinner.show();
    let t_feeder: Array<Topic> = [];
    let t_kafka: Object = {};

    const get_t_feeder = this.restApiService.getTopicList().pipe(
      mergeMap(ids => from(ids)),
      mergeMap(id => this.restApiService.getTopic(id)),
      map(t => {
        t.config = true;
        t_feeder.push(t);
      })
    );

    const get_t_kafka = this.restApiService.getAllKafkaList().pipe(
      mergeMap(ids => from(ids)),
      mergeMap(id =>
        this.restApiService
          .getTopicListFromKafka(id)
          .pipe(map(t => (t_kafka[id] = t)))
      )
    );

    const get_t_default = this.restApiService.getTopicDefault();

    forkJoin(get_t_feeder, get_t_kafka, get_t_default).subscribe(data => {
      this.columns = this.initColumn();
      this.topics = this.initRow(t_feeder, t_kafka, data[2]);
      this.t_temp = [...this.topics];
      // setTimeout(() => {
      //   //this.spinner.hide();
      //   this.loadingIndicator = false;
      // }, 500);
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
        headerName: "SETTING",
        width: "30",
        sortable: true,
        dataIndex: "config",
        icon: "check"
      },
      {
        headerIcon: "assets/icons/kibana_able.svg",
        width: "10",
        sortable: true,
        dataIndex: "kafkas"
      },
      {
        headerIcon: "assets/icons/couchbase_able.svg",
        width: "10",
        sortable: true,
        dataIndex: "sinkdbs"
      },
      {
        headerIcon: "assets/icons/druid_able.svg",
        width: "10",
        sortable: true,
        dataIndex: "sinkdbs"
      },
      {
        headerIcon: "assets/icons/elasticsearch_able.svg",
        width: "10",
        sortable: true,
        dataIndex: "sinkdbs"
      },
      {
        headerIcon: "assets/icons/mongoDB_able.svg",
        width: "10",
        sortable: true,
        dataIndex: "sinkdbs"
      },
      {
        headerIcon: "assets/icons/hadoop_able.svg",
        width: "10",
        sortable: true,
        dataIndex: "sinkdbs"
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
        width: "20",
        iconButton: "cog"
      }
    ];

    return t_columns;
  }

  initRow(t_feeder: Array<Topic>, t_kafka: Object, t_default: Topic) {
    let t_topics: Array<Topic> = [];

    // Save topics which are already configured.
    t_topics = t_feeder;

    // Save the topic which is unconfigured yet.
    Object.keys(t_kafka).forEach(k_id => {
      Object.values(t_kafka[k_id]).forEach((k_t_name: string) => {
        let found: Topic = t_feeder.find(
          t =>
            t.name == k_t_name &&
            t.kafkas.map(ids => ids.toString()).includes(k_id)
        );
        if (!found) {
          let seed: Topic;
          seed = JSON.parse(JSON.stringify(t_default));
          seed.id = null;
          seed.name = k_t_name;
          seed.kafkas = [];
          seed.kafkas.push(k_id);
          seed.config = false;
          t_topics.push(seed);
        }
      });
    });

    return t_topics;
  }

  buttonAction(string: string = "") {
    switch (string) {
      case "new":
        // Open new topic modal
        console.log("new modal");
        break;
      case "edit":
        // Open edit of topic modal
        console.log("edit modal");
        break;
      case "default":
        // Open default config of topic modal
        console.log("default modal");
        break;
      default:
        this.notificationService.success(string + " action successful!");
        break;
    }
  }

  updateFilter(searchValue) {
    const val = searchValue.toLowerCase();

    // filter our data
    const temp = this.t_temp.filter(t => {
      return t.name.toLowerCase().indexOf(val) !== -1 || !val;
    });

    // update the rows
    this.topics = temp;
  }

  // async initData() {
  //   this.topicListFeeder = [];
  //   this.topicListFeeder = await this.getTopicList("feeder");

  //   //this.topicDefaultConfig = new Topic();
  //   this.topicDefaultConfig = await this.getTopicDefaultConfig();

  //   return true;
  // }

  // getTopicList(type: string) {
  //   var data: any;

  //   switch (type) {
  //     case "feeder": {
  //       data = this.restApiService.getTopicsFromFeeder().toPromise();
  //       break;
  //     }
  //   }
  //   return data;
  // }

  // getTopicDefaultConfig() {
  //   return this.restApiService.getTopicDefaultConfig().toPromise();
  // }

  // async initTopicList(dmaapList: [], feederList: []) {
  //   // var t: Topic[] = [];
  //   // // dmaap has no topics, only show topic in db
  //   // for (var i = 0; i < feederList.length; i++) {
  //   //   let data = await this.getTopicDetail(feederList[i]);
  //   //   let dbinfo = [];
  //   //   var totalCB = 0;
  //   //   var totalDRUID = 0;
  //   //   var totalES = 0;
  //   //   var totalHDFS = 0;
  //   //   var totalMONGO = 0;
  //   //   for (var x = 0; x < data.enabledSinkdbs.length; x++) {
  //   //     let dbdata = await this.getDbDetail(data.enabledSinkdbs[x]);
  //   //     dbinfo.push(dbdata);
  //   //     if (dbinfo != undefined && dbinfo[x].type == "CB") {
  //   //       totalCB = totalCB + 1;
  //   //     } if (dbinfo != undefined && dbinfo[x].type == "DRUID") {
  //   //       totalDRUID = totalDRUID + 1;
  //   //     } if (dbinfo != undefined && dbinfo[x].type == "ES") {
  //   //       totalES = totalES + 1;
  //   //     } if (dbinfo != undefined && dbinfo[x].type == "HDFS") {
  //   //       totalHDFS = totalHDFS + 1;
  //   //     } if (dbinfo != undefined && dbinfo[x].type == "MONGO") {
  //   //       totalMONGO = totalMONGO + 1;
  //   //     }
  //   //   }
  //   //   let feed = {
  //   //     name: data.name,
  //   //     login: data.login,
  //   //     password: data.password,
  //   //     enabledSinkdbs: data.enabledSinkdbs,
  //   //     sinkdbs: data.sinkdbs,
  //   //     enabled: data.enabled,
  //   //     saveRaw: data.saveRaw,
  //   //     dataFormat: data.dataFormat,
  //   //     ttl: data.ttl,
  //   //     correlateClearedMessage: data.correlateClearedMessage,
  //   //     messageIdPath: data.messageIdPath,
  //   //     kafkas: data.kafkas.length,
  //   //     type: data.type,
  //   //     CB: totalCB,
  //   //     DRUID: totalDRUID,
  //   //     ES: totalES,
  //   //     HDFS: totalHDFS,
  //   //     MONGO: totalMONGO
  //   //   };
  //   //   t.push(feed);
  //   // }
  //   // return t;
  // }

  // onActivate(event) {
  //   const emitType = event.type;
  //   if (emitType == "dblclick") {
  //     console.log("Activate Event", event);
  //     let name = event.row.name;
  //     this.openTopicModal(name);
  //   }
  // }

  // openNewTopicModal() {
  //   const modalRef = this.modalService.open(NewTopicModelComponent, {
  //     size: "lg",
  //     centered: true
  //   });
  //   modalRef.componentInstance.newTopic = this.tempNewTopic;
  //   modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
  //     console.log(receivedEntry, "newtopic receivedEntry");
  //     this.tempNewTopic = receivedEntry;
  //     this.restApiService.addNewTopic(this.tempNewTopic).subscribe(
  //       res => {
  //         this.init();
  //         this.notificationService.success("SUCCESSFULLY_CREARED");
  //         modalRef.close();
  //         this.updateFilter(this.searchText.nativeElement.value);
  //       },
  //       err => {
  //         this.notificationService.error(err);
  //         modalRef.close();
  //         this.updateFilter(this.searchText.nativeElement.value);
  //       }
  //     );
  //   });
  // }

  // openTopicModal(name: string) {
  //   if (name == "config") {
  //     const modalRef = this.modalService.open(TopicConfigModalComponent, {
  //       windowClass: "dl-md-modal",
  //       centered: true
  //     });
  //     modalRef.componentInstance.title = "Topics Default Configurations";
  //     modalRef.componentInstance.topic = this.topicDefaultConfig;
  //     modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
  //       this.restApiService
  //         .updateTopicDefaultConfig(this.topicDefaultConfig)
  //         .subscribe(
  //           res => {
  //             this.topicDefaultConfig = receivedEntry;
  //             this.topics.forEach(t => {
  //               if (!t.type) {
  //                 // Unconfigure topics
  //                 t.login = this.topicDefaultConfig.login;
  //                 t.password = this.topicDefaultConfig.password;
  //                 t.enabledSinkdbs = this.topicDefaultConfig.enabledSinkdbs;
  //                 // t.sinkdbs = this.topicDefaultConfig.sinkdbs; //todo
  //                 t.enabled = this.topicDefaultConfig.enabled;
  //                 t.saveRaw = this.topicDefaultConfig.saveRaw;
  //                 t.dataFormat = this.topicDefaultConfig.dataFormat;
  //                 t.ttl = this.topicDefaultConfig.ttl;
  //                 t.correlateClearedMessage = this.topicDefaultConfig.correlateClearedMessage;
  //                 t.messageIdPath = this.topicDefaultConfig.messageIdPath;
  //               }
  //             });
  //             this.notificationService.success("Success updated.");
  //             modalRef.close();
  //           },
  //           err => {
  //             this.notificationService.error(err);
  //             modalRef.close();
  //           }
  //         );
  //     });
  //   } else {
  //     const index = this.temp.findIndex(t => t.name === name);
  //     const modalRef = this.modalService.open(TopicDetailModalComponent, {
  //       size: "lg",
  //       centered: true
  //     });
  //     modalRef.componentInstance.topic = this.temp[index];
  //     modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
  //       this.tempTopicDetail = receivedEntry;
  //       // Configured topic
  //       if (this.tempTopicDetail.type) {
  //         this.restApiService.getTopicsFromFeeder().subscribe(
  //           res => {
  //             if (res.find(name => name === this.tempTopicDetail.name)) {
  //               // Update topic from db
  //               this.restApiService.upadteTopic(this.tempTopicDetail).subscribe(
  //                 res => {
  //                   this.temp[index] = this.tempTopicDetail;
  //                   this.topics = this.temp;
  //                   this.notificationService.success("SUCCESSFULLY_UPDATED");
  //                   modalRef.close();
  //                   this.updateFilter(this.searchText.nativeElement.value);
  //                 },
  //                 err => {
  //                   this.notificationService.error(err);
  //                   modalRef.close();
  //                   this.updateFilter(this.searchText.nativeElement.value);
  //                 }
  //               );
  //             } else {
  //               // Insert topic from db
  //               this.restApiService.addTopic(this.tempTopicDetail).subscribe(
  //                 res => {
  //                   this.init();
  //                   this.notificationService.success("SUCCESSFULLY_CREARED");
  //                   modalRef.close();
  //                   this.updateFilter(this.searchText.nativeElement.value);
  //                 },
  //                 err => {
  //                   this.notificationService.error(err);
  //                   modalRef.close();
  //                   this.updateFilter(this.searchText.nativeElement.value);
  //                 }
  //               );
  //             }
  //           },
  //           err => {
  //             this.notificationService.error(err);
  //             modalRef.close();
  //           }
  //         );
  //       } else {
  //         // Reset to default and delete topic from db
  //         this.restApiService.deleteTopic(this.tempTopicDetail.name).subscribe(
  //           res => {
  //             this.init();
  //             this.notificationService.success("SUCCESSFULLY_DELETED");
  //             modalRef.close();
  //             this.updateFilter(this.searchText.nativeElement.value);
  //           },
  //           err => {
  //             this.notificationService.error(err);
  //             modalRef.close();
  //             this.updateFilter(this.searchText.nativeElement.value);
  //           }
  //         );
  //       }
  //     });
  //   }
  // }

  // deleteTopicModal(name: string) {
  //   const index = this.temp.findIndex(t => t.name === name);
  //   const modalRef = this.modalService.open(AlertComponent, {
  //     size: "sm",
  //     centered: true
  //   });
  //   modalRef.componentInstance.message = "ARE_YOU_SURE_DELETE";
  //   console.log(this.temp[index]);
  //   modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
  //     this.restApiService.deleteTopic(this.temp[index].name).subscribe(
  //       res => {
  //         this.init();
  //         this.notificationService.success("SUCCESSFULLY_DELETED");
  //         modalRef.close();
  //         this.updateFilter(this.searchText.nativeElement.value);
  //       },
  //       err => {
  //         this.notificationService.error(err);
  //         modalRef.close();
  //         this.updateFilter(this.searchText.nativeElement.value);
  //       }
  //     );
  //   });
  // }

  // getTopicDetail(id) {
  //   return this.restApiService.getTopicDetail(id).toPromise();
  // }

  // getDbDetail(id) {
  //   return this.restApiService.getDbDetail(id).toPromise();
  // }

  // GroupByDbType = (array, key) => {
  //   return array.reduce((result, currentValue) => {
  //     (result[currentValue.type] = result[currentValue.type] || []).push(
  //       currentValue
  //     );
  //     return result;
  //   }, {});
  // };
}
