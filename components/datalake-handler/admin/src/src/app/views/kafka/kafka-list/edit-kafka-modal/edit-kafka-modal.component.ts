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

import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";

// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";
import {Kafka} from "../../../../core/models/kafka.model";

@Component({
  selector: 'app-edit-kafka-modal',
  templateUrl: './edit-kafka-modal.component.html',
  styleUrls: ['./edit-kafka-modal.component.css']
})
export class EditKafkaModalComponent implements OnInit {
  @Input() editKafka: Kafka;
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  kafkaInput: Kafka;
  exTopicFields: Array<any> = [];
  exTopicNewField: any = {};
  protocols: Array<string> = ["SASL_PLAINTEXT"];

  constructor(
    public activeModal: NgbActiveModal,
    private spinner: NgxSpinnerService,
  ) { }

  kafkaInputTitle = "";

  ngOnInit() {
    this.kafkaInput = this.editKafka;
    if (this.kafkaInput.excludedTopic != null) {
      var excludedTopics = this.kafkaInput.excludedTopic.split(",");
      for (var i = 0; i < excludedTopics.length; i++) {
        var data = { item: excludedTopics[i] };
        this.exTopicFields.push(data);
      }
    } else {
      this.exTopicFields.push([]);
    }
    this.kafkaInputTitle = this.editKafka.name;
  }

  passBack() {
    this.spinner.show();
    if (this.kafkaInput.name == '' || this.kafkaInput.name == undefined) {
      return false;
    }
    this.editKafka = this.kafkaInput;
    this.passEntry.emit(this.editKafka);
    setTimeout(() => {
      this.spinner.hide();
    }, 500);
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
