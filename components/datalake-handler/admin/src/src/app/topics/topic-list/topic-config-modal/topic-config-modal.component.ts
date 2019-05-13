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
import { Topic } from "src/app/core/models/topic.model";

@Component({
  selector: "app-topic-config-modal",
  templateUrl: "./topic-config-modal.component.html",
  styleUrls: ["./topic-config-modal.component.css"]
})
export class TopicConfigModalComponent implements OnInit {
  @Input() topic: Topic;
  @Input() title: string;
  @Output() passEntry: EventEmitter<any> = new EventEmitter();

  // page elements
  dbs: any = [];
  dataFormats: Array<string> = ["JSON", "XML"];
  tempSeletedDbs: any = [];
  tempEnabled: boolean;
  tempSaveRaw: boolean;
  @ViewChild("t_login") t_login: ElementRef;
  @ViewChild("t_password") t_password: ElementRef;
  @ViewChild("t_dataFormat") t_dataFormat: ElementRef;
  @ViewChild("t_ttl") t_ttl: ElementRef;

  constructor(
    public activeModal: NgbActiveModal,
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
  }

  getDbs() {
    this.dbs = [];
    this.restApiService.getDbList().subscribe((data: {}) => {
      //console.log(data);
      this.dbs = data;
    });
  }

  updateSelectedDB(event: any, name: string) {
    if (event.target.checked) {
      console.log("checked");
      if (!this.tempSeletedDbs.find(db => db === name)) {
        this.tempSeletedDbs.push(name);
      }
    } else {
      console.log("unchecked");
      const index = this.tempSeletedDbs.indexOf(name, 0);
      if (index > -1) {
        this.tempSeletedDbs.splice(index, 1);
      }
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

    console.log("==================================");
    console.log("Update topic name: " + this.topic.name);
    console.log("Update topic login: " + this.topic.login);
    console.log("Update topic password: " + this.topic.password);
    console.log("Update topic sinkdbs: " + this.topic.sinkdbs);
    console.log("Update topic enabled: " + this.topic.enabled);
    console.log("Update topic saveRaw: " + this.topic.saveRaw);
    console.log("Update topic dataFormat: " + this.topic.dataFormat);
    console.log("Update topic ttl: " + this.topic.ttl);
    console.log("==================================");

    this.passEntry.emit(this.topic);
  }
}
