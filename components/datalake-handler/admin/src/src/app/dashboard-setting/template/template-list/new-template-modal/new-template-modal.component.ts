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
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { RestApiService } from "src/app/core/services/rest-api.service";
// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";

import { Template } from "src/app/core/models/template.model";

@Component({
  selector: 'app-new-template-modal',
  templateUrl: './new-template-modal.component.html',
  styleUrls: ['./new-template-modal.component.css']
})
export class NewTemplateModalComponent implements OnInit {
  @Input() template: Template;
  @Input() templatelist_length;
  templateInput: Template
  templatetypedata: Array<any> = [];
  topicname: Array<any> = [];
  dbList: Array<any> = [];
  tempSeletedDbs: any = [];
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  @ViewChild("templatetype") templatetype: ElementRef;
  @ViewChild("topic") topic: ElementRef;

  constructor(
    public activeModal: NgbActiveModal,
    public dashboardApiService: RestApiService,
    private spinner: NgxSpinnerService,
  ) { }
  inputtemplateName = null;
  templatebody = null;
  fileName = null;

  ngOnInit() {
    this.getTopicName();
    this.getDbList();
    this.getTemplateTypeName();
    // cache for display
    this.templateInput = new Template();
    const feed = {
      id: null,
      name: this.template.name,
      submitted: this.template.submitted,
      body: this.template.body,
      note: this.template.note,
      topicName: this.template.topicName,
      designType: this.template.designType,
      designTypeName: this.template.designTypeName,
      dbs: this.template.dbs || [],
    };
    this.templateInput = feed;
  }
  getTopicName() {
    this.dashboardApiService.getTopicsFromFeeder().subscribe(data => {
      this.topicname = data;
    });
  }

  getDbList() {
    this.dashboardApiService.getDbList().subscribe(data => {
      this.dbList = data;
    });
  }

  getTemplateTypeName() {
    this.dashboardApiService.getTemplateTypeName().subscribe(data => {
      this.templatetypedata = data;
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

  jsReadFiles() {
    var thiss = this;
    var file = (<HTMLInputElement>document.querySelector("#f-file")).files[0];
    this.fileName = file.name;
    var reader = new FileReader();
    reader.onload = function () {
      // console.log(this.result, "this.result");
      thiss.templateInput.body = String(this.result);
    }
    reader.readAsText(file);
  }
  passBack() {
    this.spinner.show();
    if (this.templateInput.name == '' || this.templateInput.name == undefined) {
      return false;
    }
    this.template = this.templateInput;

    // this.templatetypedata.map(item => {
    //   if (item.name === this.templatetype.nativeElement.value) {
    //     return this.template.designType = item.id;
    //   }
    // })

    this.template.designType = this.templatetypedata.filter(item => {
      return item.name === this.templatetype.nativeElement.value;
    })[0].id || "";

    this.template.designTypeName = this.templatetype.nativeElement.value;
    this.template.topicName = this.topic.nativeElement.value;
    this.template.dbs = this.tempSeletedDbs;
    this.template.submitted = false;
    this.template.note = "";
    this.passEntry.emit(this.template);
    setTimeout(() => {
      this.spinner.hide();
    }, 500);
  }
}
