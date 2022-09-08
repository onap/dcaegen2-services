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
import { NgxSpinnerService } from "ngx-spinner";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { RestApiService } from "src/app/core/services/rest-api.service";
import { AdminService } from "src/app/core/services/admin.service";
import { Db } from "src/app/core/models/db.model";
import { Template } from "src/app/core/models/template.model";
import { Topic } from "src/app/core/models/topic.model";
import { from, forkJoin } from "rxjs";
import { mergeMap, map } from "rxjs/operators";

@Component({
  selector: "app-template-modal",
  templateUrl: "./template-modal.component.html",
  styleUrls: ["./template-modal.component.css"]
})
export class TemplateModalComponent implements OnInit {
  @Input() data: Template;
  @Input() mode: string;

  designTypes: Array<any> = [];
  topics: Array<Topic> = [];
  tools: Array<Db> = [];
  fileName: string = null;

  constructor(
    public activeModal: NgbActiveModal,
    public adminService: AdminService,
    private restApiService: RestApiService,
    private spinner: NgxSpinnerService
  ) {}

  ngOnInit() {
    this.spinner.show();

    const get_designTypes = this.restApiService.getTemplateDesignType().pipe(
      mergeMap(items => from(items)),
      map(item => {
        this.designTypes.push(item);
      })
    );

    const get_topics = this.restApiService.getTopicList().pipe(
      mergeMap(ids => from(ids)),
      mergeMap(id => this.restApiService.getTopic(id)),
      map(t => {
        this.topics.push(t);
      })
    );

    const get_tools = this.restApiService.getAllTools().pipe(
      mergeMap(tools => from(tools)),
      map(tool => {
        this.tools.push(tool);
      })
    );

    forkJoin(get_designTypes, get_topics, get_tools).subscribe(data => {
      setTimeout(() => {
        this.spinner.hide();
      }, 500);
    });
  }

  jsReadFiles() {
    let thiss = this;
    let file = (<HTMLInputElement>document.querySelector("#f-file")).files[0];
    this.fileName = file.name;
    let reader = new FileReader();
    reader.onload = function() {
      thiss.data.body = String(this.result);
    };
    reader.readAsText(file);
  }
}
