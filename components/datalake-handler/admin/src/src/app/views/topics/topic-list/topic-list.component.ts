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
import { AlertComponent } from "../../../shared/components/alert/alert.component";

@Component({
  selector: "app-topic-list",
  templateUrl: "./topic-list.component.html",
  styleUrls: ["./topic-list.component.css"]
})
export class TopicListComponent {
  topicListDmaap: any = [];
  topicListFeeder: any = [];
  topicDefaultConfig: Topic;

  topics: Topic[] = [];
  temp: Topic[] = []; // cache for topics
  tempTopicDetail: Topic; // temp for a topic
  tempNewTopic: Topic; // temp for a newtopic

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
  @ViewChild("mydatatable") topicTable;

  constructor(
    private restApiService: RestApiService,
    private modalService: NgbModal,
    private notificationService: ToastrNotificationService,
    private spinner: NgxSpinnerService
  ) {
    setTimeout(() => {
      this.loadingIndicator = false;
    }, 5000);
    this.init()

  }

  ngOnInit() {
    this.spinner.show();
  }

  init() {
    this.initData().then(data => {
      this.initTopicList(this.topicListDmaap, this.topicListFeeder).then(
        data => {
          // for cache of datatable
          this.temp = [...data];
          this.topics = data;
          setTimeout(() => {
            this.spinner.hide();
          }, 500);
        }
      );
    });
  }

  async initData() {

    this.topicListFeeder = [];
    this.topicListFeeder = await this.getTopicList("feeder");

    this.topicDefaultConfig = new Topic();
    this.topicDefaultConfig = await this.getTopicDefaultConfig();

    return true;
  }

  getTopicList(type: string) {
    var data: any;

    switch (type) {
      case "feeder": {
        data = this.restApiService.getTopicsFromFeeder().toPromise();
        break;
      }
    }
    return data;
  }

  getTopicDefaultConfig() {
    return this.restApiService.getTopicDefaultConfig().toPromise();
  }

  async initTopicList(dmaapList: [], feederList: []) {
    var t: Topic[] = [];

      // dmaap has no topics, only show topic in db
      for (var i = 0; i < feederList.length; i++) {
        let data = await this.getTopicDetail(feederList[i]);
        let dbinfo = [];
        var totalCB = 0;
        var totalDRUID = 0;
        var totalES = 0;
        var totalHDFS = 0;
        var totalMONGO = 0;
        for (var x = 0; x < data.enabledSinkdbs.length; x++) {
          let dbdata = await this.getDbDetail(data.enabledSinkdbs[x]);
          dbinfo.push(dbdata);
          if (dbinfo!=undefined && dbinfo[x].type=="CB"){
            totalCB = totalCB + 1;
          }if (dbinfo!=undefined && dbinfo[x].type=="DRUID"){
            totalDRUID = totalDRUID + 1;
          }if (dbinfo!=undefined && dbinfo[x].type=="ES"){
            totalES = totalES + 1;
          }if (dbinfo!=undefined && dbinfo[x].type=="HDFS"){
            totalHDFS = totalHDFS + 1;
          }if (dbinfo!=undefined && dbinfo[x].type=="MONGO"){
            totalMONGO = totalMONGO + 1;
          }
        }

        let feed = {
          name: data.name,
          login: data.login,
          password: data.password,
          enabledSinkdbs: data.enabledSinkdbs,
          sinkdbs: data.sinkdbs,
          enabled: data.enabled,
          saveRaw: data.saveRaw,
          dataFormat: data.dataFormat,
          ttl: data.ttl,
          correlateClearedMessage: data.correlateClearedMessage,
          messageIdPath: data.messageIdPath,
          kafkas: data.kafkas.length,
          type: data.type,
          CB: totalCB,
          DRUID: totalDRUID,
          ES: totalES,
          HDFS: totalHDFS,
          MONGO: totalMONGO
        };
        t.push(feed);
      }

    return t;
  }

  onActivate(event) {
    const emitType = event.type;
    if (emitType == "dblclick") {
      console.log('Activate Event', event);
      let name = event.row.name;
      this.openTopicModal(name);
    }

  }

  openNewTopicModal() {
    const modalRef = this.modalService.open(NewTopicModelComponent, {
      size: "lg",
      centered: true
    });
    modalRef.componentInstance.newTopic = this.tempNewTopic;
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      console.log(receivedEntry, "newtopic receivedEntry");
      this.tempNewTopic = receivedEntry;
      this.restApiService.addNewTopic(this.tempNewTopic).subscribe(
        res => {
          this.init();
          this.notificationService.success("SUCCESSFULLY_CREARED");
          modalRef.close();
          this.updateFilter(this.searchText.nativeElement.value);
        },
        err => {
          this.notificationService.error(err);
          modalRef.close();
          this.updateFilter(this.searchText.nativeElement.value);
        }
      );
    })


  }

  openTopicModal(name: string) {
    if (name == "config") {
      const modalRef = this.modalService.open(TopicConfigModalComponent, {
        windowClass: "dl-md-modal",
        centered: true
      });
      modalRef.componentInstance.title = "Topics Default Configurations";
      modalRef.componentInstance.topic = this.topicDefaultConfig;
      modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
        this.restApiService
          .updateTopicDefaultConfig(this.topicDefaultConfig)
          .subscribe(
            res => {
              this.topicDefaultConfig = receivedEntry;
              this.topics.forEach(t => {
                if (!t.type) {
                  // Unconfigure topics
                  t.login = this.topicDefaultConfig.login;
                  t.password = this.topicDefaultConfig.password;
                  t.enabledSinkdbs = this.topicDefaultConfig.enabledSinkdbs;
                  // t.sinkdbs = this.topicDefaultConfig.sinkdbs; //todo
                  t.enabled = this.topicDefaultConfig.enabled;
                  t.saveRaw = this.topicDefaultConfig.saveRaw;
                  t.dataFormat = this.topicDefaultConfig.dataFormat;
                  t.ttl = this.topicDefaultConfig.ttl;
                  t.correlateClearedMessage = this.topicDefaultConfig.correlateClearedMessage;
                  t.messageIdPath = this.topicDefaultConfig.messageIdPath;
                }
              });
              this.notificationService.success("Success updated.");
              modalRef.close();
            },
            err => {
              this.notificationService.error(err);
              modalRef.close();
            }
          );
      });
    } else {
      const index = this.temp.findIndex(t => t.name === name);
      const modalRef = this.modalService.open(TopicDetailModalComponent, {
        size: "lg",
        centered: true
      });
      modalRef.componentInstance.topic = this.temp[index];
      modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
        this.tempTopicDetail = receivedEntry;
        // Configured topic
        if (this.tempTopicDetail.type) {
          this.restApiService.getTopicsFromFeeder().subscribe(
            res => {
              if (res.find(name => name === this.tempTopicDetail.name)) {
                // Update topic from db
                this.restApiService.upadteTopic(this.tempTopicDetail).subscribe(
                  res => {
                    this.temp[index] = this.tempTopicDetail;
                    this.topics = this.temp;
                    this.notificationService.success("SUCCESSFULLY_UPDATED");
                    modalRef.close();
                    this.updateFilter(this.searchText.nativeElement.value);
                  },
                  err => {
                    this.notificationService.error(err);
                    modalRef.close();
                    this.updateFilter(this.searchText.nativeElement.value);
                  }
                );
              } else {
                // Insert topic from db
                this.restApiService.addTopic(this.tempTopicDetail).subscribe(
                  res => {
                    this.init();
                    this.notificationService.success("SUCCESSFULLY_CREARED");
                    modalRef.close();
                    this.updateFilter(this.searchText.nativeElement.value);
                  },
                  err => {
                    this.notificationService.error(err);
                    modalRef.close();
                    this.updateFilter(this.searchText.nativeElement.value);
                  }
                );
              }
            },
            err => {
              this.notificationService.error(err);
              modalRef.close();
            }
          );
        } else {
          // Reset to default and delete topic from db
          this.restApiService.deleteTopic(this.tempTopicDetail.name).subscribe(
            res => {
              this.init();
              this.notificationService.success("SUCCESSFULLY_DELETED");
              modalRef.close();
              this.updateFilter(this.searchText.nativeElement.value);
            },
            err => {
              this.notificationService.error(err);
              modalRef.close();
              this.updateFilter(this.searchText.nativeElement.value);
            }
          );
        }
      });
    }
  }

  deleteTopicModal(name: string) {
    const index = this.temp.findIndex(t => t.name === name);
    const modalRef = this.modalService.open(AlertComponent, {
      size: "sm",
      centered: true
    });
    modalRef.componentInstance.message = "ARE_YOU_SURE_DELETE";
    console.log(this.temp[index]);
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      this.restApiService.deleteTopic(this.temp[index].name).subscribe(
        res => {
          this.init();
          this.notificationService.success("SUCCESSFULLY_DELETED");
          modalRef.close();
          this.updateFilter(this.searchText.nativeElement.value);
        },
        err => {
          this.notificationService.error(err);
          modalRef.close();
          this.updateFilter(this.searchText.nativeElement.value);
        }
      );

    })
  }

  getTopicDetail(id) {
    return this.restApiService.getTopicDetail(id).toPromise();
  }

  getDbDetail(id) {
    return this.restApiService.getDbDetail(id).toPromise();
  }

  GroupByDbType = (array, key) => {
    return array.reduce((result, currentValue) => {
      (result[currentValue.type] = result[currentValue.type] || []).push(currentValue);
      return result;
    }, {});
  };

  updateFilter(searchValue) {
    const val = searchValue.toLowerCase();

    // filter our data
    const temp = this.temp.filter(function (d) {
      return d.name.toLowerCase().indexOf(val) !== -1 || !val;
    });

    // update the rows
    this.topics = temp;
  }
}