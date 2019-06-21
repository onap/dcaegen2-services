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
import {
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter,
  ViewChild,
  ElementRef
} from "@angular/core";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {RestApiService} from "src/app/core/services/rest-api.service";
import {AdminService} from "src/app/core/services/admin.service";
import {newTopic} from "src/app/core/models/topic.model";

@Component({
  selector: 'app-new-topic-model',
  templateUrl: './new-topic-model.component.html',
  styleUrls: ['./new-topic-model.component.css']
})
export class NewTopicModelComponent implements OnInit {
  @Input() newTopic: newTopic;
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  TopicInput: newTopic;
  // page elements
  dbs: any = [];
  dataFormats: Array<string> = ["JSON", "XML"];
  tempSeletedDbs: any = [];
  idExFields: Array<any> = [];
  idExNewField: any = {};
  @ViewChild("t_topicname") t_topicname: ElementRef;
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
    this.newTopic = {
      name: "",
      login: "",
      password:"",
      sinkdbs: [],
      enabled: false,
      saveRaw: false,
      dataFormat: this.dataFormats[0],
      ttl: null,
      correlateClearedMessage: false,
      messageIdPath: null,
    };
    this.TopicInput = new newTopic();
    const feeds = {
        name: this.newTopic.name,
        login: this.newTopic.login,
        password:this.newTopic.password,
        sinkdbs: this.newTopic.sinkdbs,
        enabled: this.newTopic.enabled,
        saveRaw: this.newTopic.saveRaw,
        dataFormat: this.newTopic.dataFormat,
        ttl: this.newTopic.ttl,
        correlateClearedMessage: this.newTopic.correlateClearedMessage,
        messageIdPath: this.newTopic.messageIdPath,
      };
    this.TopicInput = feeds;
    this. idExFields = [];
    if (this.TopicInput.messageIdPath != null) {
      var feed = this.TopicInput.messageIdPath.split(",");
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
    this.newTopic = this.TopicInput;
    this.newTopic.name = this.t_topicname.nativeElement.value;
    this.newTopic.login = this.t_login.nativeElement.value;
    this.newTopic.password = this.t_password.nativeElement.value;
    this.newTopic.sinkdbs = this.tempSeletedDbs;
    this.newTopic.dataFormat = this.t_dataFormat.nativeElement.value;
    this.newTopic.ttl = this.t_ttl.nativeElement.value;
    this.newTopic.messageIdPath = "";
    for (var i = 0; i < this.idExFields.length; i++) {
      let item = "/"+this.idExFields[i].item;
      if (i == 0) {
        this.newTopic.messageIdPath = item;
      } else {
        this.newTopic.messageIdPath =
          this.newTopic.messageIdPath + "," + item;
      }
    }
    // Reset to default
    if (this.newTopic.sinkdbs.length == 0) {
      return false;
    }
    console.log(this.newTopic);
    this.passEntry.emit(this.newTopic);
  }

}
