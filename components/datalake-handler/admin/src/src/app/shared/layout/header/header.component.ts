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

import { Component, Output } from "@angular/core";
import { AdminService } from "../../../core/services/admin.service";
import { TranslateService } from "@ngx-translate/core";
import { Feeder } from "src/app/core/models/feeder.model";
import { RestApiService } from "src/app/core/services/rest-api.service";

// notify
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";

@Component({
  selector: "app-header",
  templateUrl: "./header.component.html",
  styleUrls: ["./header.component.css"]
})
export class HeaderComponent {
  title = "PageTitle";
  feeder: any = [];

  selectedLang: String;
  langs: Array<any> = [
    { value: "en-us", name: "EN" },
    { value: "zh-hans", name: "中文(简)" },
    { value: "zh-hant", name: "中文(繁)" }
  ];

  constructor(
    private adminService: AdminService,
    private restApiService: RestApiService,
    private notificationService: ToastrNotificationService,
    private translateService: TranslateService
  ) {
    this.translateService.setDefaultLang("en-us");
  }

  ngOnInit() {
    this.adminService.title.subscribe(title => {
      this.title = title;
    });
    this.selectedLang = this.translateService.defaultLang;

    if ((this.title = "SIDEBAR.FEEDER")) {
      this.restApiService.getFeederstatus().subscribe((data: {}) => {
        this.feeder = new Feeder();
        this.feeder = data;
      });
    }
  }

  changeLanguage(lang: string) {
    this.translateService.use(lang);
  }

  changeFeederStatus() {
    if (this.feeder.running) {
      this.restApiService.startFeeder().subscribe(
        res => {
          this.notificationService.success("Success start feeder.");
        },
        err => {
          this.feeder.running = false;
          this.notificationService.error(err);
        }
      );
    } else {
      this.restApiService.stopFeeder().subscribe(
        res => {
          this.notificationService.success("Success stop feeder.");
        },
        err => {
          this.feeder.running = true;
          this.notificationService.error(err);
        }
      );
    }
  }

  /*
   *  Feeder
   */
  getFeederStatus() {
    return this.restApiService.getFeederstatus().toPromise();
  }

  setFeederStatus(status: string) {
    var data;

    switch (status) {
      case "start":
        data = true;
        console.log("start feeder");
        break;
      case "stop":
        data = false;
        console.log("stop feeder");
        break;
    }

    return data;
  }
}
