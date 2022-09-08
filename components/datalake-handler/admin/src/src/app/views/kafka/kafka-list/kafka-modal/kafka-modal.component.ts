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

import { Component, OnInit, Input } from "@angular/core";

import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { AdminService } from "src/app/core/services/admin.service";
import { Kafka } from "src/app/core/models/kafka.model";

@Component({
  selector: "app-kafka-modal",
  templateUrl: "./kafka-modal.component.html",
  styleUrls: ["./kafka-modal.component.css"]
})
export class KafkaModalComponent implements OnInit {
  @Input() data: Kafka;
  @Input() mode: string;

  securityProtocol: Array<string> = ["None", "SASL_PLAINTEXT"];
  extenFields: Array<any> = [];
  newField: any = {};

  constructor(
    public activeModal: NgbActiveModal,
    public adminService: AdminService
  ) {}

  ngOnInit() {
    // Get excludedTopic field
    this.extenFields = [];
    if (this.data.excludedTopic != null) {
      let feed = this.data.excludedTopic.split(",");
      for (let i = 0; i < feed.length; i++) {
        let data = { item: feed[i] };
        this.extenFields.push(data);
      }
    } else {
      this.extenFields.push([]);
    }
  }

  onClickAddIdField() {
    this.extenFields.push(this.newField);
    this.newField = {};
    this.onChangeSaveIdField();
  }

  onClickDelIdField(index: number) {
    if (this.extenFields.length > 1) {
      this.extenFields.splice(index, 1);
      this.onChangeSaveIdField();
    }
  }

  onChangeSaveIdField() {
    this.data.excludedTopic = "";

    for (let i = 0; i < this.extenFields.length; i++) {
      if (this.extenFields[i].item) {
        if (this.data.excludedTopic == "") {
          this.data.excludedTopic = this.extenFields[i].item;
        } else {
          this.data.excludedTopic += "," + this.extenFields[i].item;
        }
      }
    }
  }
}
