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
import { DashboardApiService } from "src/app/core/services/dashboard-api.service";
import { AdminService } from "src/app/core/services/admin.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import { Template,newTemplate} from "src/app/core/models/template.model";
import {AlertComponent} from "../../../../core/alert/alert.component";

@Component({
  selector: 'app-new-template-modal',
  templateUrl: './new-template-modal.component.html',
  styleUrls: ['./new-template-modal.component.css']
})
export class NewTemplateModalComponent implements OnInit {
  @Input() template: newTemplate;
  @Input() templatelist_length;
  @Input() selectedLangs;
  templateInput: newTemplate
  templatetypedata: Array<any> = [];
  topicname: Array<any> = [];
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  // @ViewChild("inputtemplateName") inputtemplateName: ElementRef;
  // @ViewChild("templatebody") templatebody: ElementRef;
  @ViewChild("templatetype") templatetype: ElementRef;
  @ViewChild("topic") topic: ElementRef;

  constructor(
    public activeModal: NgbActiveModal,
    public dashboardApiService: DashboardApiService,
    private modalService: NgbModal,

  ) { }
  inputtemplateName = null;
  templatebody = null;

  ngOnInit() {
    this.getTopicName();
    this.getTemplateTypeName();
    // cache for display
    this.templateInput = new Template();
    const feed = {
      name: this.template.name,
      submitted: this.template.submitted,
      body: this.template.body,
      note: this.template.note,
      topic: this.template.topic,
      designType: this.template.designType
    };
    this.templateInput = feed;
  }
  getTopicName(){
    this.dashboardApiService.getTopicName().subscribe(data => {
      this.topicname = data;
      console.log(this.topicname,"this.topicname")
    });
  }

  getTemplateTypeName(){
    this.dashboardApiService.getTemplateTypeName().subscribe(data => {
      this.templatetypedata = data;
      console.log(this.templatetypedata,"this.templatetypedata")
    });
  }

  passBack() {
    if(this.templateInput.name == '' || this.templateInput.name == undefined){
      let tips = "";
      if (this.selectedLangs == "en-us") {
        tips = "Name input is required."
      } else if (this.selectedLangs == "zh-hans") {
        tips = "名字输入项是必填项。"
      } else if (this.selectedLangs == "zh-hant") {
        tips = "名字輸入項是必填項。"
      }
      return false;
    }
    this.template = this.templateInput;
    console.log(this.templateInput);
    this.template.designType = this.templatetype.nativeElement.value;
    this.template.topic = this.topic.nativeElement.value;
    // this.template.id = this.templatelist_length+1;
    this.template.submitted = true;
    this.template.note = "";
    console.log(this.template);
    this.passEntry.emit(this.template);
  }
}
