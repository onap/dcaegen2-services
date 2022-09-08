/*
    Copyright (C) 2019 - 2020 CMCC, Inc. and others. All rights reserved.

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

/**
 * @author Chunmeng Guo
 * @contributor Ekko Chang
 */

import { Component, OnInit } from "@angular/core";
import { RestApiService } from "src/app/core/services/rest-api.service";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { Kafka } from "src/app/core/models/kafka.model";

// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";

import { map, mergeMap } from "rxjs/operators";
import { from, forkJoin } from "rxjs";

// Notify
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";
import { AlertComponent } from "src/app/shared/components/alert/alert.component";
import { ModalContentData } from "src/app/shared/modules/modal/modal.data";
import { ModalComponent } from "src/app/shared/modules/modal/modal.component";
import { KafkaModalComponent } from "src/app/views/kafka/kafka-list/kafka-modal/kafka-modal.component";

@Component({
  selector: "app-kafka-list",
  templateUrl: "./kafka-list.component.html",
  styleUrls: ["./kafka-list.component.css"]
})
export class KafkaListComponent implements OnInit {
  kafkas: Array<Kafka> = [];

  // app-card parameters
  cardModifiable: boolean = true;
  cardAddiconPath: string = "assets/icons/add.svg";

  constructor(
    private restApiService: RestApiService,
    private notificationService: ToastrNotificationService,
    private modalService: NgbModal,
    private spinner: NgxSpinnerService
  ) {}

  ngOnInit() {
    this.spinner.show();
    let t_kafkas: Array<Kafka> = [];

    const get_kafkas = this.restApiService.getAllKafka().pipe(
      mergeMap(ks => from(ks)),
      map(k => {
        if (k.enabled == true) {
          k.iconPath = "assets/icons/kafka_able.svg";
        } else {
          k.iconPath = "assets/icons/kafka_disable.svg";
        }
        t_kafkas.push(k);
      })
    );

    forkJoin(get_kafkas).subscribe(data => {
      this.kafkas = t_kafkas;
      setTimeout(() => {
        this.spinner.hide();
      }, 500);
    });
  }

  cardMoreClickAction(mode: string, k: Kafka) {
    switch (mode) {
      case "edit":
        this.openModal("edit", k);
        break;
      case "delete":
        const modalRef = this.modalService.open(AlertComponent, {
          size: "sm",
          centered: true,
          backdrop: "static"
        });
        modalRef.componentInstance.message = "ARE_YOU_SURE_DELETE";
        modalRef.componentInstance.passEntry.subscribe(recevicedEntry => {
          this.restApiService.deleteKafka(k.id).subscribe(
            res => {
              this.ngOnInit();
              setTimeout(() => {
                this.notificationService.success("SUCCESSFULLY_DELETED");
              }, 500);
            },
            err => {
              this.notificationService.error(err);
            }
          );
          modalRef.close();
        });
        break;
    }
  }

  openModal(mode: string, k: Kafka) {
    const modalRef = this.modalService.open(ModalComponent, {
      size: "lg",
      centered: true,
      backdrop: "static"
    });

    switch (mode) {
      case "new":
        let newKafka: Kafka;
        let componentNew = new ModalContentData(KafkaModalComponent, newKafka);

        modalRef.componentInstance.title = "NEW_KAFKA";
        modalRef.componentInstance.notice = "";
        modalRef.componentInstance.mode = "new";
        modalRef.componentInstance.component = componentNew;

        modalRef.componentInstance.passEntry.subscribe((data: Kafka) => {
          newKafka = Object.assign({}, data);
          this.restApiService.addKafka(newKafka).subscribe(
            res => {
              this.ngOnInit();
              setTimeout(() => {
                this.notificationService.success("SUCCESSFULLY_CREARED");
              }, 500);
            },
            err => {
              this.notificationService.error(err);
            }
          );
          modalRef.close();
        });
        break;
      case "edit":
        let editKafka: Kafka = k;
        let componentEdit = new ModalContentData(
          KafkaModalComponent,
          editKafka
        );

        modalRef.componentInstance.title = editKafka.name;
        modalRef.componentInstance.notice = "";
        modalRef.componentInstance.mode = "edit";
        modalRef.componentInstance.component = componentEdit;

        modalRef.componentInstance.passEntry.subscribe((data: Kafka) => {
          editKafka = Object.assign({}, data);
          this.restApiService.updateKafka(editKafka).subscribe(
            res => {
              this.ngOnInit();
              setTimeout(() => {
                this.notificationService.success("SUCCESSFULLY_UPDATED");
              }, 500);
            },
            err => {
              this.notificationService.error(err);
            }
          );
          modalRef.close();
        });
        break;
    }
  }
}
