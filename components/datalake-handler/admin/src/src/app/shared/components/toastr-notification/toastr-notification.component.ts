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
 * This component is to display the notification.
 *
 * @author Ekko Chang
 *
 */

import { Component, OnInit } from "@angular/core";
import {
  Notification,
  NotificationType
} from "src/app/core/models/toastr-notification.model";
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";

@Component({
  selector: "app-toastr-notification",
  templateUrl: "./toastr-notification.component.html",
  styleUrls: ["./toastr-notification.component.css"]
})
export class ToastrNotificationComponent implements OnInit {
  notifications: Notification[] = [];

  constructor(public _notificationService: ToastrNotificationService) { }

  ngOnInit() {
    this._notificationService.getAlert().subscribe((alert: Notification) => {
      this.notifications = [];
      if (!alert) {
        this.notifications = [];
        return;
      }
      this.notifications.push(alert);
      setTimeout(() => {
        this.notifications = this.notifications.filter(x => x !== alert);
      }, 5000);
    });
  }

  removeNotification(notification: Notification) {
    this.notifications = this.notifications.filter(x => x !== notification);
  }

  /*
    Set css class for Alert -- Called from alert component
  */
  cssClass(notification: Notification) {
    if (!notification) {
      return;
    }
    switch (notification.type) {
      case NotificationType.Success:
        return "toast-success";
      case NotificationType.Error:
        return "toast-error";
      case NotificationType.Info:
        return "toast-info";
      case NotificationType.Warning:
        return "toast-warning";
    }
  }
}
