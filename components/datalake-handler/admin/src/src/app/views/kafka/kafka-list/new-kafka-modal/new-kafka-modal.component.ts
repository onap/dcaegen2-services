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

import {Component, EventEmitter, Input, OnInit, Output, ElementRef, ViewChild} from '@angular/core';

import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { RestApiService } from "src/app/core/services/rest-api.service";
import { NgxSpinnerService } from "ngx-spinner";

import { Kafka } from "../../../../core/models/kafka.model";

@Component({
  selector: 'app-new-kafka-modal',
  templateUrl: './new-kafka-modal.component.html',
  styleUrls: ['./new-kafka-modal.component.css']
})
export class NewKafkaModalComponent implements OnInit {
  @Input() kafka: Kafka;
  @Input() kafkaList_length;
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  kafkaInput: Kafka;
  exTopicFields: Array<any> = [];
  exTopicNewField: any = {};
  protocols: Array<string> = ["SASL_PLAINTEXT"];

  @ViewChild("k_name") k_name: ElementRef;
  @ViewChild("k_login") k_login: ElementRef;
  @ViewChild("k_pass") k_pass: ElementRef;
  @ViewChild("k_enabled") k_enabled: ElementRef;
  @ViewChild("k_brokerList") k_brokerList: ElementRef;
  @ViewChild("k_zooKeeper") k_zooKeeper: ElementRef;
  @ViewChild("k_group") k_group: ElementRef;
  @ViewChild("k_secure") k_secure: ElementRef;
  @ViewChild("k_securityProtocol") k_securityProtocol: ElementRef;
  @ViewChild("k_includedTopic") k_includedTopic: ElementRef;
  @ViewChild("k_excludedTopic") k_excludedTopic: ElementRef;
  @ViewChild("k_consumerCount") k_consumerCount: ElementRef;
  @ViewChild("k_timeout") k_timeout: ElementRef;

  constructor(
    private activeModal: NgbActiveModal,
    private kafkaApiService: RestApiService,
    private spinner: NgxSpinnerService
  ) { }

  ngOnInit() {
    // cache for display
    this.kafkaInput = new Kafka();
    const feed = {
      id: null,
      name: this.kafka.name,
      enabled: this.kafka.enabled,
      brokerList: this.kafka.brokerList,
      zooKeeper: this.kafka.zooKeeper,
      group: this.kafka.group,
      secure: this.kafka.secure,
      login: this.kafka.login,
      pass: this.kafka.pass,
      securityProtocol: this.kafka.securityProtocol,
      includedTopic: this.kafka.includedTopic,
      excludedTopic: this.kafka.excludedTopic,
      consumerCount: this.kafka.consumerCount,
      timeout: this.kafka.timeout
    };
    this.kafkaInput = feed;
    this.exTopicFields = [];
    if (this.kafkaInput.excludedTopic != null) {
      var feeds = this.kafkaInput.excludedTopic.split(",");
      for (var i = 0; i < feeds.length; i++) {
        var data = { item: feed[i] };
        this.exTopicFields.push(data);
      }
    } else {
      this.exTopicFields.push([]);
    }
  }

  passBack() {
    this.spinner.show();
    if (this.kafkaInput.name == '' || this.kafkaInput.name == undefined) {
      return false;
    }
    this.kafka = this.kafkaInput;
    this.kafka.securityProtocol = this.k_securityProtocol.nativeElement.value;
    for (var i = 0; i < this.exTopicFields.length; i++) {
      let item = this.exTopicFields[i].item;
      if (i == 0) {
        this.kafka.excludedTopic = item;
      } else {
        this.kafka.excludedTopic = this.kafka.excludedTopic + "," + item;
      }
    }
    console.log(this.kafka);
    this.passEntry.emit(this.kafka);

  }

  addExTopicField() {
    this.exTopicFields.push(this.exTopicNewField);
    this.exTopicNewField = {};
  }

  deleteExTopicField(index: number) {
    if (this.exTopicFields.length > 1) {
      this.exTopicFields.splice(index, 1);
    }
  }
}
