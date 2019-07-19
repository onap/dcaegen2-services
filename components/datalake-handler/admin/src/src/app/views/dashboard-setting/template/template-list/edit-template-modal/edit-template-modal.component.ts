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
import { Template } from "src/app/core/models/template.model";

// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";

@Component({
  selector: 'app-edit-template-modal',
  templateUrl: './edit-template-modal.component.html',
  styleUrls: ['./edit-template-modal.component.css']
})
export class EditTemplateModalComponent implements OnInit {
  @Input() edittemplate: Template;
  templateInput: Template;
  defaultDesigntype: String;
  defaultTopicname: String;
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
  templateInputTitle = "";
  fileName = null;

  ngOnInit() {
    // cache for display
    this.templateInput = new Template();
    this.getDbList();
    const feed = {
      id: this.edittemplate.id,
      name: this.edittemplate.name,
      submitted: this.edittemplate.submitted,
      body: this.edittemplate.body,
      note: this.edittemplate.note,
      topicName: this.edittemplate.topicName,
      designType: this.edittemplate.designType,
      designTypeName: this.edittemplate.designTypeName,
      dbs: this.edittemplate.dbs,
    };
    this.templateInput = feed;
    this.templateInputTitle = "" + this.edittemplate.name;
    this.tempSeletedDbs = this.templateInput.dbs.map(item => { return Number(item) });

    this.getTopicName();
    this.getTemplateTypeName();
  }
  getTopicName() {
    this.dashboardApiService.getTopicsFromFeeder().subscribe(data => {
      this.topicname = data;
    });
  }

  getDbList() {
    this.dashboardApiService.getTempDbList().subscribe(data => {
      Object.keys(data).map(item => {
        this.dbList.push({ key: item, name: data[item] })
      })
    });
  }

  updateSelectedDB(event: any, name: any) {
    if (event.target.checked) {
      if (!this.tempSeletedDbs.find(db => db === name)) {
        this.tempSeletedDbs.push(Number(name.key));
      }
    } else {
      const index = this.tempSeletedDbs.indexOf(+name.key, 0);
      if (index > -1) {
        this.tempSeletedDbs.splice(index, 1);
      }
    }
  }

  getTemplateTypeName() {
    this.dashboardApiService.getTemplateTypeName().subscribe(data => {
      this.templatetypedata = data;
      this.getDefaultOptions();
    });
  }

  getDefaultOptions() {
    this.templatetypedata.map(item => {
      if (item.id === this.templateInput.designType) {
        return this.defaultDesigntype = item.name;
      }
    });
    this.defaultTopicname = this.templateInput.topicName;
  }

  jsReadFiles() {
    var thiss = this;
    var file = (<HTMLInputElement>document.querySelector("#f-file")).files[0];
    this.fileName = file.name;
    var reader = new FileReader();
    reader.onload = function () {
      thiss.templateInput.body = String(this.result);
    }
    reader.readAsText(file);
  }

  passBack() {
    this.spinner.show();
    if (this.templateInput.name == '' || this.templateInput.name == undefined) {
      return false;
    }
    this.edittemplate = this.templateInput;
    // this.templatetypedata.map(item => {
    //   if (item.name === this.templatetype.nativeElement.value) {
    //     return this.edittemplate.designType = item.id;
    //   }
    // });
    this.edittemplate.designType = this.templatetypedata.filter(item => {
      return item.name === this.templatetype.nativeElement.value;
    })[0].id || "";

    this.edittemplate.designTypeName = this.templatetype.nativeElement.value;
    this.edittemplate.topicName = this.topic.nativeElement.value;
    this.edittemplate.dbs = this.tempSeletedDbs;
    this.passEntry.emit(this.edittemplate);
    setTimeout(() => {
      this.spinner.hide();
    }, 500);
  }
}
