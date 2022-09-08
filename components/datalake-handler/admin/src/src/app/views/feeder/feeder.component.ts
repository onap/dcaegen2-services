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

import { Component, OnInit } from "@angular/core";
import { AdminService } from "src/app/core/services/admin.service";
import { RestApiService } from "src/app/core/services/rest-api.service";
import { HeaderComponent } from "src/app/shared/layout/header/header.component";

// notify
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";

// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";

@Component({
  selector: "app-feeder",
  templateUrl: "./feeder.component.html",
  styleUrls: ["./feeder.component.css"]
})
export class FeederComponent implements OnInit {
  topicContent: string = "";

  constructor(
    private adminService: AdminService,
    private restApiService: RestApiService,
    private notificationService: ToastrNotificationService,
    private spinner: NgxSpinnerService
  ) {
    this.adminService.setTitle("SIDEBAR.FEEDER");
    this.restApiService.getTopicsFromFeeder().subscribe(
      res => {
        this.topicContent = res.length.toString();
        setTimeout(() => {
          this.spinner.hide();
        }, 500);
      },
      err => {
        this.topicContent = "No Data";
        this.notificationService.error(err);
        setTimeout(() => {
          this.spinner.hide();
        }, 500);
      }
    );
  }

  ngOnInit() {
    this.spinner.show();
  }

  chkTopicContent() {
    if (this.topicContent == "No Data") {
      return false;
    } else {
      return true;
    }
  }
}
