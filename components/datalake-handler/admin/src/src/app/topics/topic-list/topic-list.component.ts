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

// notify
import { ToastrNotificationService } from "src/app/core/services/toastr-notification.service";

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

  loadingIndicator: boolean = true;
  mesgNoData = {
    // {emptyMessage: `<div></div> 'No Data' | translate, selectedMessage: false}`
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
    private restApiService: RestApiService,
    private modalService: NgbModal,
    private notificationService: ToastrNotificationService
  ) {
    setTimeout(() => {
      this.loadingIndicator = false;
    }, 5000);

    this.initData().then(data => {
      this.initTopicList(this.topicListDmaap, this.topicListFeeder).then(
        data => {
          // for cache of datatable
          this.temp = [...data];
          this.topics = data;
        }
      );
    });
  }

  ngOnInit() {}

  async initData() {
    this.topicListDmaap = [];
    this.topicListDmaap = await this.getTopicList("dmaap");
    console.log("topic list from dmaap: " + this.topicListDmaap);

    this.topicListFeeder = [];
    this.topicListFeeder = await this.getTopicList("feeder");
    console.log("topic list from feeder: " + this.topicListFeeder);

    this.topicDefaultConfig = new Topic();
    this.topicDefaultConfig = await this.getTopicDefaultConfig();
    console.log("topic default config name: " + this.topicDefaultConfig.name);

    return true;
  }

  getTopicList(type: string) {
    var data: any;

    switch (type) {
      case "dmaap": {
        data = this.restApiService.getTopicsFromDmaap().toPromise();
        break;
      }
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

    // dmaap has topics
    if (dmaapList.length > 0) {
      for (var i = 0; i < dmaapList.length; i++) {
        if (feederList.includes(dmaapList[i])) {
          let data = await this.getTopicDetail(dmaapList[i]);
          let feed = {
            name: dmaapList[i],
            login: data.login,
            password: data.password,
            sinkdbs: data.sinkdbs,
            enabled: data.enabled,
            saveRaw: data.saveRaw,
            dataFormat: data.dataFormat,
            ttl: data.ttl,
            correlateClearedMessage: data.correlateClearedMessage,
            messageIdPath: data.messageIdPath,
            type: true
          };
          t.push(feed);
        } else {
          let feed = {
            name: dmaapList[i],
            login: this.topicDefaultConfig.login,
            password: this.topicDefaultConfig.password,
            sinkdbs: this.topicDefaultConfig.sinkdbs,
            enabled: this.topicDefaultConfig.enabled,
            saveRaw: this.topicDefaultConfig.saveRaw,
            dataFormat: this.topicDefaultConfig.dataFormat,
            ttl: this.topicDefaultConfig.ttl,
            correlateClearedMessage: this.topicDefaultConfig
              .correlateClearedMessage,
            messageIdPath: this.topicDefaultConfig.messageIdPath,
            type: false
          };
          t.push(feed);
        }
      }
    } else {
      // dmaap has no topics, only show topic in db
      for (var i = 0; i < feederList.length; i++) {
        let data = await this.getTopicDetail(feederList[i]);
        let feed = {
          name: feederList[i],
          login: data.login,
          password: data.password,
          sinkdbs: data.sinkdbs,
          enabled: data.enabled,
          saveRaw: data.saveRaw,
          dataFormat: data.dataFormat,
          ttl: data.ttl,
          correlateClearedMessage: data.correlateClearedMessage,
          messageIdPath: data.messageIdPath,
          type: true
        };
        t.push(feed);
      }
    }

    return t;
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
                    this.notificationService.success("Success updated.");
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
                    this.notificationService.success("Success inserted.");
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
              this.temp[index].enabled = this.topicDefaultConfig.enabled;
              this.temp[index].login = this.topicDefaultConfig.login;
              this.temp[index].password = this.topicDefaultConfig.password;
              this.temp[index].sinkdbs = this.topicDefaultConfig.sinkdbs;
              this.temp[index].dataFormat = this.topicDefaultConfig.dataFormat;
              this.temp[index].ttl = this.topicDefaultConfig.ttl;
              this.temp[index].saveRaw = this.topicDefaultConfig.saveRaw;
              this.temp[
                index
              ].correlateClearedMessage = this.topicDefaultConfig.correlateClearedMessage;
              this.temp[
                index
              ].messageIdPath = this.topicDefaultConfig.messageIdPath;
              this.temp[index].type = false;

              this.topics = this.temp;
              this.notificationService.success("Success deleted.");
              modalRef.close();
              this.updateFilter(this.searchText.nativeElement.value);
            },
            err => {
              this.notificationService.error(err);
              modalRef.close();
              this.updateFilter(this.searchText.nativeElement.value);
            }
          );

          // temp code
          // this.topics[index] = this.tempTopicDetail;
          // this.temp[index] = this.topics[index];
          // this.notificationService.success("Success deleted.");
          // modalRef.close();
        }
      });
    }
  }

  getTopicDetail(name: string) {
    return this.restApiService.getTopicDetail(name).toPromise();
  }

  updateFilter(searchValue) {
    const val = searchValue.toLowerCase();

    // filter our data
    const temp = this.temp.filter(function(d) {
      return d.name.toLowerCase().indexOf(val) !== -1 || !val;
    });

    // update the rows
    this.topics = temp;
  }
}
