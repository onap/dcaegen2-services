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

import { Component, OnInit } from "@angular/core";
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";

import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { ModalComponent } from "src/app/shared/modules/modal/modal.component";

import { Topic } from "src/app/core/models/topic.model";
import { ModalContentData } from "src/app/shared/modules/modal/modal.data";
import { ModalDemoComponent } from "src/app/views/test/modal-demo/modal-demo.component";

@Component({
  selector: "app-test",
  templateUrl: "./test.component.html",
  styleUrls: ["./test.component.css"]
})
export class TestComponent implements OnInit {
  modalShow: boolean = false;
  mockcolumns: Array<any> = [];
  mocktabledata: Array<any> = [];

  // Modal example
  topic: Topic;
  // Modal example end

  // Card example
  cardTitle: string;
  cardIconPath: string;
  cardContent: string;
  cardSubcontent: string;
  cardModifiable: boolean;
  cardAddicon: string;
  // Card example end

  constructor(
    private notificationService: ToastrNotificationService,
    // Modal example
    private modalService: NgbModal // Modal example end
  ) {}

  ngOnInit() {
    this.mockcolumns = [
      {
        headerName: "STATUS",
        width: "20",
        dataIndex: "enabled",
        sortable: true,
        icon: "status"
      },
      {
        headerName: "2",
        width: "100",
        dataIndex: "type",
        icon: "status"
      },
      {
        headerName: "3",
        width: "200",
        dataIndex: "topic",
        renderText: "zzzz"
      },
      {
        headerIcon: "assets/icons/couchbase_able.svg",
        width: "200",
        dataIndex: "cc",
        renderText: "zzzzz"
      },
      {
        headerName: "5",
        width: "50",
        dataIndex: "",
        textButton: "DEPLOY1"
      },
      {
        width: "20",
        iconButton: "cog"
      },
      {
        headerName: "Configured",
        width: "100",
        dataIndex: "configured",
        icon: "check"
      },
      {
        headerName: "TTL",
        width: "100",
        dataIndex: "saveRaw",
        icon: "check"
      }
    ];
    this.mocktabledata = [
      {
        name: "aaaa",
        type: "333",
        topic: "尽快尽快",
        cc: "123",
        configured: true,
        saveRaw: true,
        enabled: true
      },
      {
        name: "ccccc",
        type: "666",
        topic: "2222",
        cc: "222",
        configured: true,
        saveRaw: true,
        enabled: false
      },
      {
        name: "bbbbb",
        type: "77777",
        topic: "555",
        cc: "5",
        configured: false,
        saveRaw: false,
        enabled: false
      }
    ];

    // Modal example
    // Data for different components of modal body
    // Example for topic, not only topic but also db, design or tools
    // this.topic = new Topic();
    // this.topic.name = "topic.name (test)";
    // this.topic.login = "123";
    // this.topic.password = "123";
    // this.topic.sinkdbs = "";
    // this.topic.enabled = true;
    // this.topic.saveRaw = true;
    // this.topic.dataFormat = "";
    // this.topic.ttl = 123;
    // this.topic.correlateClearedMessage = true;
    // this.topic.messageIdPath = "";
    // this.topic.type = false;
    // Modal example end

    // Card example
    this.cardTitle = "Topics";
    // Display content as string
    this.cardContent = "1234";
    // or display content as svg-icons(path)
    this.cardIconPath = "assets/icons/couchbase_able.svg";
    this.cardSubcontent = "QCT-Kafka";
    this.cardModifiable = true;

    this.cardAddicon = "assets/icons/add.svg";
    // Card example end
  }

  buttonAction(string: string = "") {
    switch (string) {
      case "modal":
        this.modalShow = true;
        break;
      case "modalcancel":
        this.modalShow = false;
        break;
      default:
        this.notificationService.success(string + " action successful!");
        break;
    }
  }

  openModalDemo() {
    let contentComponent = new ModalContentData(ModalDemoComponent, {
      title: "AAI-EVENT-TEST", // Modal title string
      notice: "Notice: This topic uses the default topics settings.", // Notice string
      state: "new", // Modal type: new/edit
      content: this.topic // Data for modal body in different component
    });

    const modalRef = this.modalService.open(ModalComponent, {
      size: "lg",
      centered: true
    });

    modalRef.componentInstance.contentComponent = contentComponent;
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      console.log("receivedEntry: " + receivedEntry);
      modalRef.close();
    });
  }

  cardMoreAction($event) {
    if ($event == "edit") {
      this.openModalDemo();
    } else {
      console.log($event, "$event");
    }
  }
  cardClick() {
    this.openModalDemo();
  }
}
