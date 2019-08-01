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
import { Component,EventEmitter, OnInit, Output } from '@angular/core';
import { kafka } from "../../core/models/kafka.model";
import { AdminService } from "../../core/services/admin.service";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";

import { RestApiService } from "src/app/core/services/rest-api.service";

// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";
import {ToastrNotificationService} from "../../shared/components/toastr-notification/toastr-notification.service";
@Component({
  selector: 'app-kafka',
  templateUrl: './kafka.component.html',
  styleUrls: ['./kafka.component.css']
})
export class KafkaComponent implements OnInit {
  kafkaList: any = [];
  kafkas: kafka[] = [];

  cardIconPath: string;
  cardModifiable: boolean;
  cardAddicon: string;

  constructor(
    private adminService: AdminService,
    private kafkaApiService: RestApiService,
    private notificationService: ToastrNotificationService,
    private modalService: NgbModal,
    private spinner: NgxSpinnerService
  ) {
    // Set page title
    this.adminService.setTitle("SIDEBAR.KAFKA");
    this.initList();
  }

  ngOnInit() {
    this.spinner.show();
    this.cardIconPath = "assets/icons/couchbase_able.svg";
    this.cardModifiable = true;
    this.cardAddicon = "assets/icons/add.svg";
  }
  initList() {
    this.initData().then(data => {
      this.initDbsList(this.kafkaList).then(data => {
        this.kafkas = data;
        console.log(this.kafkas, "kafkas[]")
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
    var data: any;
    data = this.kafkaApiService.getAllKafkaList().toPromise();
    return data;
  }

  async initDbsList(kafkaList: []) {
    var k: kafka[] = [];

    if (kafkaList.length > 0) {
      for (var i = 0; i < kafkaList.length; i++) {
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


}
