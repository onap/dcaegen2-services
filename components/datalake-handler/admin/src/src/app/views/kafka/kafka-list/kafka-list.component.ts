/*
    Copyright (C) 2019 CMCC, Inc. and others. All rights reserved.

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
 * @author Chunmeng Guo
 */

import { Component, OnInit, ElementRef } from '@angular/core';
import { RestApiService } from "src/app/core/services/rest-api.service";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { Kafka } from "../../../core/models/kafka.model";

// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";

// notify
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";
import {AlertComponent} from "../../../shared/components/alert/alert.component";
import {NewKafkaModalComponent} from "./new-kafka-modal/new-kafka-modal.component";
import {ModalContentData} from "../../../shared/modules/modal/modal.data";
import {ModalDemoComponent} from "../../test/modal-demo/modal-demo.component";
import {ModalComponent} from "../../../shared/modules/modal/modal.component";
import {EditKafkaModalComponent} from "./edit-kafka-modal/edit-kafka-modal.component";
import {el} from "@angular/platform-browser/testing/src/browser_util";

@Component({
  selector: 'app-kafka-list',
  templateUrl: './kafka-list.component.html',
  styleUrls: ['./kafka-list.component.css']
})
export class KafkaListComponent implements OnInit {

  kafkaList: any = [];
  kafkas: Kafka[] = [];
  cardIconPath: string;
  cardModifiable: boolean;
  cardAddicon: string;
  Kafka_New: Kafka;
  Kafka_Newbody: Kafka;
  cardIconPathList: any = [];

  constructor(
    private kafkaApiService: RestApiService,
    private notificationService: ToastrNotificationService,
    private modalService: NgbModal,
    private spinner: NgxSpinnerService
  ) {
    this.initList();
  }

  ngOnInit() {
    this.spinner.show();
    this.cardModifiable = true;
    this.cardAddicon = "assets/icons/add.svg";
  }

  initList() {
    this.initData().then(data => {
      this.initKafkasList(this.kafkaList).then(data => {
        this.kafkas = data;
        if (this.kafkas.length > 0) {
          let a = "assets/icons/kafka_able.svg";
          let b = "assets/icons/kafka_disable.svg";
          for (let i = 0; i < this.kafkas.length; i++) {
            this.cardIconPath = (this.kafkas[i].enabled == true) ? a : b;
            this.cardIconPathList.push(this.cardIconPath);
          }
        }
        console.log(this.cardIconPathList, "kafkas[]");
      });
    });
  }

  async initData() {
    this.kafkaList = [];
    this.kafkaList = await this.getKafkaList();
    setTimeout(() => {
      this.spinner.hide();
    }, 500);
  }

  getKafkaList() {
    let data: any;
    data = this.kafkaApiService.getAllKafkaList().toPromise();
    return data;
  }

  async initKafkasList(kafkaList: []) {
    let k: Kafka[] = [];
    if (kafkaList.length > 0) {
      for (let i = 0; i < kafkaList.length; i++) {
        let data = kafkaList[i];
        let feed = {
          id: data["id"],
          name: data["name"],
          enabled: data["enabled"],
          brokerList: data["brokerList"],
          zooKeeper: data["zooKeeper"],
          group: data["group"],
          secure: data["secure"],
          login: data["login"],
          pass: data["pass"],
          securityProtocol: data["securityProtocol"],
          includedTopic: data["includedTopic"],
          excludedTopic: data["excludedTopic"],
          consumerCount: data["consumerCount"],
          timeout: data["timeout"]
        };
        k.push(feed);
      }
    }
    return k;
  }

  deleteKafkaModel(id: number) {
    const index = this.kafkaList.findIndex(t => t.id === id);
    const modalRef = this.modalService.open(AlertComponent, {
      size: "sm",
      centered: true
    });
    modalRef.componentInstance.message = "ARE_YOU_SURE_DELETE";
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      // Delete kafka
      this.kafkaApiService.deleteKafka(id).subscribe(
        res => {
          console.log(res);
          if (JSON.stringify(res).length <= 2) {
            this.kafkaList.splice(index, 1);
            this.kafkaList = [...this.kafkaList];
            this.initList();
            this.notificationService.success("SUCCESSFULLY_DELETED");

          } else {
            this.initList();
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

  newKafkaModal() {
    const modalRef = this.modalService.open(NewKafkaModalComponent, {
      windowClass: "dl-md-modal kafkas",
      centered: true
    });

    this.Kafka_New = new Kafka();
    this.Kafka_Newbody = new Kafka();
    modalRef.componentInstance.kafka = this.Kafka_Newbody;
    modalRef.componentInstance.kafkaList_length = this.kafkaList.length;
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      this.Kafka_Newbody = receivedEntry;
      this.kafkaApiService
        .createNewKafka(this.Kafka_Newbody)
        .subscribe(
          res => {
            this.spinner.hide();
            if (res.statusCode == 200) {
              this.Kafka_New = res.returnBody;
              this.kafkaList.push(this.Kafka_New);
              this.kafkaList = [...this.kafkaList];
              this.initList();
              this.notificationService.success("SUCCESSFULLY_CREARED");
            } else {
              this.initList();
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

  cardMoreAction($event, id) {

    if($event == "edit"){
      this.editKafkaModal(id);
    }else {
      console.log($event,id);
      this.deleteKafkaModel(id);
    }
  }

  editKafkaModal(id: number) {
    console.log("id", id)
    const index = this.kafkaList.findIndex(t => t.id === id);
    const modalRef = this.modalService.open(EditKafkaModalComponent, {
      windowClass: "dl-md-modal kafkas",
      centered: true
    });
    modalRef.componentInstance.editKafka = this.kafkaList[index];
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      this.Kafka_New = receivedEntry;
      this.kafkaApiService
        .updateKafka(this.Kafka_New)
        .subscribe(
          res => {
            if (res.statusCode == 200) {
              this.kafkaList[index] = this.Kafka_New;
              this.kafkaList = [...this.kafkaList];
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
    })
  }
}
