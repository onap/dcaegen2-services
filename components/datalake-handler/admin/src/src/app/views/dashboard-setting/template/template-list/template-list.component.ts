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
 *
 * @constructor Ekko Chang
 */

import { Component, OnInit, ViewChild, ElementRef } from "@angular/core";
import { RestApiService } from "src/app/core/services/rest-api.service";
import { Template } from "src/app/core/models/template.model";

import { AlertComponent } from "src/app/shared/components/alert/alert.component";
import { ModalComponent } from "src/app/shared/modules/modal/modal.component";
import { ModalContentData } from "src/app/shared/modules/modal/modal.data";
import { TemplateModalComponent } from "src/app/views/dashboard-setting/template/template-list/template-modal/template-modal.component";
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";

import { NgxSpinnerService } from "ngx-spinner";
import { map, mergeMap } from "rxjs/operators";
import { forkJoin, from } from "rxjs";

@Component({
  selector: "app-template-list",
  templateUrl: "./template-list.component.html",
  styleUrls: ["./template-list.component.css"]
})
export class TemplateListComponent {
  columns: Array<any> = [];
  templates: Array<Template> = [];
  t_temp: Array<any> = []; // cache for templates

  @ViewChild("searchText") searchText: ElementRef;

  constructor(
    private notificationService: ToastrNotificationService,
    private modalService: NgbModal,
    private restApiService: RestApiService,
    private spinner: NgxSpinnerService
  ) {}

  ngOnInit() {
    this.spinner.show();

    let t_templates: Array<Template> = [];

    const get_templates = this.restApiService.getAllTemplate().pipe(
      mergeMap(templates => from(templates)),
      map(template => {
        t_templates.push(template);
      })
    );

    forkJoin(get_templates).subscribe(data => {
      this.columns = this.initColumn();
      this.templates = t_templates;
      this.t_temp = [...this.templates];
      this.updateFilter(this.searchText.nativeElement.value);
      setTimeout(() => {
        this.spinner.hide();
      }, 500);
    });
  }

  initColumn() {
    let t_columns: Array<any> = [];

    t_columns = [
      {
        headerName: "NAME",
        width: "420",
        sortable: true,
        dataIndex: "name"
      },
      {
        headerName: "Type",
        width: "100",
        sortable: true,
        dataIndex: "designTypeName"
      },
      {
        headerName: "Topics name",
        width: "200",
        sortable: true,
        dataIndex: "topicName"
      },
      {
        headerName: "Deploy to dashboard",
        width: "80",
        textButton: "DEPLOY",
        action: "deploy"
      },
      {
        width: "2",
        iconButton: "cog",
        action: "edit"
      },
      {
        width: "2",
        iconButton: "trash",
        action: "delete"
      }
    ];

    return t_columns;
  }

  updateFilter(searchValue: string) {
    const val = searchValue.toLowerCase();

    // filter our data
    const temp = this.t_temp.filter(t => {
      return t.name.toLowerCase().indexOf(val) !== -1 || !val;
    });

    // update the rows
    this.templates = temp;
  }

  btnTableAction(passValueArr: Array<any>) {
    let action = passValueArr[0];
    let id = passValueArr[1];

    switch (action) {
      case "edit":
        this.openModal("edit", id);
        break;
      case "delete":
        const modalRef = this.modalService.open(AlertComponent, {
          size: "sm",
          centered: true,
          backdrop: "static"
        });
        modalRef.componentInstance.message = "ARE_YOU_SURE_DELETE";
        modalRef.componentInstance.passEntry.subscribe(recevicedEntry => {
          this.restApiService.deleteTemplate(id).subscribe(
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
      case "deploy":
        this.spinner.show();
        let index: number = this.templates.findIndex(d => d.id === id);
        let data: Template = this.templates[index];

        this.restApiService.deployTemplateKibana(id, data).subscribe(
          res => {
            setTimeout(() => {
              this.spinner.hide();
            }, 500);

            let processArr = [];
            Object.keys(res).map(item =>
              processArr.push({ name: item, status: res[item] })
            );

            if (processArr.length > 0) {
              processArr.map(item =>
                item.status
                  ? setTimeout(() => {
                      this.notificationService.success("Deploy_SUCCESSFULLY");
                    }, 600)
                  : setTimeout(() => {
                      this.notificationService.error("Deploy_FAILED");
                    }, 600)
              );
            } else {
              this.notificationService.error("Deploy_FAILED");
            }
          },
          err => {
            setTimeout(() => {
              this.spinner.hide();
            }, 500);
            this.notificationService.error(err);
          }
        );
        break;
    }
  }

  openModal(mode: string = "", id: number | string) {
    const modalRef = this.modalService.open(ModalComponent, {
      size: "lg",
      centered: true,
      backdrop: "static"
    });

    switch (mode) {
      case "new":
        let newTemplate: Template = new Template();
        newTemplate.submitted = false;
        let componentNew = new ModalContentData(
          TemplateModalComponent,
          newTemplate
        );

        modalRef.componentInstance.title = "NEW_TEMPLATE";
        modalRef.componentInstance.notice = "";
        modalRef.componentInstance.mode = "new";
        modalRef.componentInstance.component = componentNew;

        modalRef.componentInstance.passEntry.subscribe((data: Template) => {
          newTemplate = Object.assign({}, data);
          newTemplate.dbs = new Array();
          newTemplate.dbs.push(data.dbs[0]);
          this.restApiService.addTemplate(newTemplate).subscribe(
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
        let index: number = this.templates.findIndex(db => db.id === id);
        let editTemplate: Template = this.templates[index];
        let componentEdit = new ModalContentData(
          TemplateModalComponent,
          editTemplate
        );

        modalRef.componentInstance.title = editTemplate.name;
        modalRef.componentInstance.notice = "";
        modalRef.componentInstance.mode = "edit";
        modalRef.componentInstance.component = componentEdit;

        modalRef.componentInstance.passEntry.subscribe((data: Template) => {
          editTemplate = Object.assign({}, data);
          editTemplate.dbs = new Array();
          editTemplate.dbs.push(data.dbs[0]);
          this.restApiService.updateTemplate(editTemplate).subscribe(
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
