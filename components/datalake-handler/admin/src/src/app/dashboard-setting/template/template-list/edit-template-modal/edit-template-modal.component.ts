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
import { Template } from "src/app/core/models/template.model";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AlertComponent} from "../../../../core/alert/alert.component";

@Component({
  selector: 'app-edit-template-modal',
  templateUrl: './edit-template-modal.component.html',
  styleUrls: ['./edit-template-modal.component.css']
})
export class EditTemplateModalComponent implements OnInit {
  @Input() edittemplate: Template;
  templateInput: Template;
  templatetypedata: Array<any> = [];
  topicname: Array<any> = [];
  @Output() passEntry: EventEmitter<any> = new EventEmitter();

  @ViewChild("templatetype") templatetype: ElementRef;
  @ViewChild("topic") topic: ElementRef;

  constructor(
    public activeModal: NgbActiveModal,
    public dashboardApiService: DashboardApiService,
    private modalService: NgbModal,
  ) { }

  inputtemplateName = null;
  templateInputTitle = "";
  fileName = null;

  ngOnInit() {
    this.getTopicName();
    this.getTemplateTypeName();
    // cache for display
    this.templateInput = new Template();
    const feed = {
      id:this.edittemplate.id,
      name: this.edittemplate.name,
      submitted: this.edittemplate.submitted,
      body: this.edittemplate.body,
      note: this.edittemplate.note,
      topic: this.edittemplate.topic,
      designType:  this.edittemplate.designType,
      display: this.edittemplate.display
    };
    this.templateInput = feed;
    this.templateInputTitle = ""+this.edittemplate.name;
    console.log(this.templateInput,"this.templateInput")
  }
  getTopicName(){
    this.dashboardApiService.getTopicName().subscribe(data => {
      this.topicname = data;
    });
  }

  getTemplateTypeName(){
    this.dashboardApiService.getTemplateTypeName().subscribe(data => {
      this.templatetypedata = data;
    });
  }

  jsReadFiles(){
    var thiss =this;
    var file = (<HTMLInputElement>document.querySelector("#f-file")).files[0];
    this.fileName = file.name;
    var reader = new FileReader();
    reader.onload = function() {
      console.log(this.result);
      thiss.templateInput.body = String(this.result);
    }
    reader.readAsText(file);
  }

  passBack() {
    if(this.templateInput.name == '' || this.templateInput.name == undefined){
      return false;
    }
    console.log(this.templateInput);
    this.edittemplate = this.templateInput;
    this.edittemplate.display = this.templatetype.nativeElement.value;
    this.edittemplate.designType = this.templatetypedata.find((item) => {
      return item.display == this.edittemplate.display
    }).designType;
    this.edittemplate.topic = this.topic.nativeElement.value;
    this.passEntry.emit(this.edittemplate);
  }
}
