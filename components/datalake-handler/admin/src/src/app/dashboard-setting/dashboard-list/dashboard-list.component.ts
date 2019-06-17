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
import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {Dashboard} from "../../core/models/dashboard.model";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CreateDashboardComponent} from "./create-dashboard/create-dashboard.component";

import {AdminService} from "../../core/services/admin.service";

// DB modal components
import {DashboardApiService} from "src/app/core/services/dashboard-api.service";

import {AlertComponent} from "src/app/core/alert/alert.component";

// Notify
import {ToastrNotificationService} from "src/app/core/services/toastr-notification.service";
// Loading spinner
import {NgxSpinnerService} from "ngx-spinner";

@Component({
  selector: 'app-dashboard-list',
  templateUrl: './dashboard-list.component.html',
  styleUrls: ['./dashboard-list.component.css']
})
export class DashboardListComponent implements OnInit {
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  dbList: any = [];
  dbs: Dashboard[] = [];

  loading: Boolean = true;

  tempDbDetail: Dashboard;
  selectedLangs = sessionStorage.getItem("selectedLang") || "en-us";
  dashboardDeteleModelShow = true;

  // nameArr = [];

  constructor(
    private adminService: AdminService,
    private dashboardApiService: DashboardApiService,
    private notificationService: ToastrNotificationService,
    private modalService: NgbModal,
    private spinner: NgxSpinnerService
  ) {
    // Set page title
    this.adminService.setTitle("SIDEBAR.DASHBOARDLIST");
    // this.getName();
    this.initData().then(data => {
      this.initDbsList(this.dbList).then(data => {
        this.dbs = data;
        console.log(this.dbs, "dasboard-dbs[]")
      });
    });
  }

  ngOnInit() {
    this.spinner.show();
  }


  async initData() {
    this.dbList = [];
    this.dbList = await this.getDbList();
    setTimeout(() => {
      this.spinner.hide();
    }, 500);
  }

  getDbList() {
    var data: any;
    data = this.dashboardApiService.getDashboardList().toPromise();
    return data;
  }

  async initDbsList(dbList: []) {
    var d: Dashboard[] = [];

    if (dbList.length > 0) {
      for (var i = 0; i < dbList.length; i++) {
        let data = dbList[i];
        let feed = {
          name: data["name"],
          host: data["host"],
          port: data["port"],
          login: data["login"],
          pass: data["pass"],
          enabled: data["enabled"]
        };
        d.push(feed);
      }
    }
    return d;
  }

  openDashboardModal(thisIndex: number) {
    var modalRef, index;
    this.selectedLangs = sessionStorage.getItem("selectedLang") || "en-us";
    let tips = "";
    index = thisIndex;
    console.log(index, "index,add or edit");
    modalRef = this.modalService.open(CreateDashboardComponent, {
      size: "lg",
      centered: true
    });
    modalRef.componentInstance.dashboard = this.dbs[index];
    modalRef.componentInstance.passEntry.subscribe(receiveEntry => {
      this.dbs[index] = receiveEntry;
      let host = this.dbs[index].host;
      let enabled = this.dbs[index].enabled;
      console.log(receiveEntry);
      if (enabled == true) {
        // Db name found, to update db
        this.dashboardApiService.createUpadteDashboard(this.dbs[index]).subscribe(
          res => {
            console.log(res);
            if (res.statusCode == 200) {
              this.initData();
              if (this.selectedLangs == "en-us") {
                tips = "Success updated."
              } else if (this.selectedLangs == "zh-hans") {
                tips = "更新成功。"
              } else if (this.selectedLangs == "zh-hant") {
                tips = "更新成功。"
              }
              this.notificationService.success('"' + host + '"' + tips);
            } else {
              if (this.selectedLangs == "en-us") {
                tips = "Fail updated."
              } else if (this.selectedLangs == "zh-hans") {
                tips = "更新失败。"
              } else if (this.selectedLangs == "zh-hant") {
                tips = "更新失敗。"
              }
              this.notificationService.error('"' + host + '"' + tips);
            }
            modalRef.close();
          },
          err => {
            this.notificationService.error(err);
            modalRef.close();
          }
        );
      } else {
        this.dashboardApiService.deleteDashboard(this.dbs[thisIndex]).subscribe(
          res => {
            console.log(res);
            if (res.statusCode == 200) {
              this.initData();
              if (this.selectedLangs == "en-us") {
                tips = "Success deleted."
              } else if (this.selectedLangs == "zh-hans") {
                tips = "删除成功。"
              } else if (this.selectedLangs == "zh-hant") {
                tips = "刪除成功。"
              }
              this.notificationService.success('"' + host + '"' + tips);
            } else {
              this.dbs[thisIndex].enabled = true;
              if (this.selectedLangs == "en-us") {
                tips = "Fail deleted."
              } else if (this.selectedLangs == "zh-hans") {
                tips = "删除失败。"
              } else if (this.selectedLangs == "zh-hant") {
                tips = "刪除失敗。"
              }
              this.notificationService.error('"' + host + '"' + tips);
            }
            modalRef.close();
          },
          err => {
            this.notificationService.error(err);
            modalRef.close();
          }
        );

      }

    });
  }
}
