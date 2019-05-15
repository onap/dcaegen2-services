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

import {
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter,
  ViewChild,
  ElementRef
} from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { RestApiService } from "src/app/core/services/rest-api.service";
import { AdminService } from "src/app/core/services/admin.service";
import { Topic } from "src/app/core/models/topic.model";

@Component({
  selector: "app-topic-detail-modal",
  templateUrl: "./topic-detail-modal.component.html",
  styleUrls: ["./topic-detail-modal.component.css"]
})
export class TopicDetailModalComponent implements OnInit {
  @Input() topic: Topic;
  @Output() passEntry: EventEmitter<any> = new EventEmitter();

  // page elements
  dbs: any = [];
  dataFormats: Array<string> = ["JSON", "XML"];
  tempSeletedDbs: any = [];
  tempEnabled: boolean;
  tempSaveRaw: boolean;
  tempMsg: boolean;
  idExFields: Array<any> = [];
  idExNewField: any = {};
  @ViewChild("t_login") t_login: ElementRef;
  @ViewChild("t_password") t_password: ElementRef;
  @ViewChild("t_dataFormat") t_dataFormat: ElementRef;
  @ViewChild("t_ttl") t_ttl: ElementRef;

  constructor(
    public activeModal: NgbActiveModal,
    public adminService: AdminService,
    private restApiService: RestApiService
  ) {
    this.getDbs();
  }
  ngOnInit() {
    // for display
    this.topic.sinkdbs.forEach(item => {
      this.tempSeletedDbs.push(item);
    });
    this.tempEnabled = this.topic.enabled;
    this.tempSaveRaw = this.topic.saveRaw;
    this.tempMsg = this.topic.correlateClearedMessage;
    this.idExFields = [];

    if (this.topic.messageIdPath != null) {
      var feed = this.topic.messageIdPath.split(",");
      for (var i = 0; i < feed.length; i++) {
        var data = { item: feed[i] };
        this.idExFields.push(data);
      }
    } else {
      this.idExFields.push([]);
    }
  }

  getDbs() {
    this.dbs = [];
    this.restApiService.getDbList().subscribe((data: {}) => {
      this.dbs = data;
    });
  }

  updateSelectedDB(event: any, name: string) {
    if (event.target.checked) {
      if (!this.tempSeletedDbs.find(db => db === name)) {
        this.tempSeletedDbs.push(name);
      }
    } else {
      const index = this.tempSeletedDbs.indexOf(name, 0);
      if (index > -1) {
        this.tempSeletedDbs.splice(index, 1);
      }
    }
  }

  addIdField() {
    this.idExFields.push(this.idExNewField);
    this.idExNewField = {};
  }

  deleteIdField(index: number) {
    if (this.idExFields.length > 1) {
      this.idExFields.splice(index, 1);
    }
  }

  passBack() {
    this.topic.enabled = this.tempEnabled;
    this.topic.login = this.t_login.nativeElement.value;
    this.topic.password = this.t_password.nativeElement.value;

    this.topic.sinkdbs = this.tempSeletedDbs;

    this.topic.dataFormat = this.t_dataFormat.nativeElement.value;
    this.topic.ttl = this.t_ttl.nativeElement.value;
    this.topic.saveRaw = this.tempSaveRaw;
    this.topic.correlateClearedMessage = this.tempMsg;
    this.topic.messageIdPath = "";
    for (var i = 0; i < this.idExFields.length; i++) {
      if (i == 0) {
        this.topic.messageIdPath = this.idExFields[i].item;
      } else {
        this.topic.messageIdPath =
          this.topic.messageIdPath + "," + this.idExFields[i].item;
      }
    }

    // Reset to default
    if (this.topic.sinkdbs.length == 0) {
      this.topic.type = false;
    } else {
      this.topic.type = true;
    }
    this.passEntry.emit(this.topic);
  }
}
