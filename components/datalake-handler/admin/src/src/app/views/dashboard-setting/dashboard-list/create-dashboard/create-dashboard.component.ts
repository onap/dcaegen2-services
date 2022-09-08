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
import { Component, OnInit, Input, Output, ViewChild, EventEmitter, ElementRef } from '@angular/core';
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { Dashboard } from "src/app/core/models/dashboard.model";
import { AdminService } from "src/app/core/services/admin.service";
import { RestApiService } from "src/app/core/services/rest-api.service";
@Component({
  selector: 'app-create-dashboard',
  templateUrl: './create-dashboard.component.html',
  styleUrls: ['./create-dashboard.component.css']
})
export class CreateDashboardComponent implements OnInit {

  constructor(
    public activeModal: NgbActiveModal,
    public adminService: AdminService,
    public restApiService: RestApiService,
  ) { }

  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  @Input() dashboard: Dashboard;
  @Input() nameArr;
  tempDb: Dashboard;

  selectshow = false;
  tempDbNameTitle = null;

  @ViewChild("t_dataDashboardName") t_dataDashboardName: ElementRef;

  ngOnInit() {
    // cache for display

    console.log(this.dashboard);
    this.tempDb = new Dashboard();
    if (this.dashboard.enabled == undefined) {
      this.dashboard.enabled = true;
    }
    const feeds = {
      name: this.dashboard.name,
      host: this.dashboard.host,
      port: this.dashboard.port,
      login: this.dashboard.login,
      pass: this.dashboard.pass,
      enabled: this.dashboard.enabled,
    };
    console.log(feeds);
    this.tempDb = feeds;
    this.tempDbNameTitle = this.dashboard.host
  }



  passBack() {
    if (this.tempDb.host == null && this.tempDb.port == null && this.tempDb.login == null && this.tempDb.pass == null) {
      return false;
    }
    this.dashboard = this.tempDb;
    this.dashboard.name = this.t_dataDashboardName.nativeElement.value;
    console.log(this.dashboard, "this.dashboard output");
    this.passEntry.emit(this.dashboard);
  }

}
