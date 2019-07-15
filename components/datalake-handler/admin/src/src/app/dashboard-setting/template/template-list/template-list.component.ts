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
import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { RestApiService } from "src/app/core/services/rest-api.service";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { Template } from "src/app/core/models/template.model";
// Loading spinner
import { NgxSpinnerService } from "ngx-spinner";

// modal
import { NewTemplateModalComponent } from "./new-template-modal/new-template-modal.component";
import { EditTemplateModalComponent } from "./edit-template-modal/edit-template-modal.component";
import { AlertComponent } from "src/app/core/alert/alert.component";
// notify
import { ToastrNotificationService } from "src/app/core/services/toastr-notification.service";

@Component({
  selector: 'app-template-list',
  templateUrl: './template-list.component.html',
  styleUrls: ['./template-list.component.css']
})
export class TemplateListComponent {
  template_list: any = [];
  templates: Template[] = [];
  temps: Template[] = [];
  Template_New: Template;
  Template_Newbody: Template;
  dashboardDeteleModelShow = true;
  loadingIndicator: boolean = true;
  mesgNoData = {
    emptyMessage: `
      <div class="d-flex justify-content-center">
        <div class="p-2">
          <label class="dl-nodata">No Data</label>
        </div>
      </div>
    `
  };
  @ViewChild("searchText") searchText: ElementRef;
  constructor(
    private modalService: NgbModal,
    private dashboardApiService: RestApiService,
    private spinner: NgxSpinnerService,
    private notificationService: ToastrNotificationService,
  ) {
    setTimeout(() => {
      this.loadingIndicator = false;
    }, 5000);

    this.initData().then(data => {
      this.initTemplateList(this.template_list).then(
        data => {
          // for cache of datatable
          this.temps = [...data];
          this.templates = data;
          setTimeout(() => {
            this.spinner.hide();
          }, 500);
        }
      );
    });
  }

  ngOnInit() {
    this.spinner.show();
  }

  async initData() {
    this.template_list = [];
    this.template_list = await this.getTemplateList();
    this.Template_New = new Template();
    this.Template_Newbody = new Template();
    return true;
  }


  getTemplateList() {
    return this.dashboardApiService.getTemplateAll().toPromise();
  }

  async initTemplateList(template_list: []) {
    var t: Template[] = [];
    for (var i = 0; i < template_list.length; i++) {
      let data = template_list[i];
      let feed = {
        id: data["id"],
        name: data["name"],
        submitted: data["submitted"],
        body: data["body"],
        note: data["note"],
        topicName: data["topicName"],
        designType: data["designType"],
        designTypeName: data["designTypeName"],
      };
      t.push(feed);
    }
    return t;
  }

  newTemplateModal() {
    const modalRef = this.modalService.open(NewTemplateModalComponent, {
      windowClass: "dl-md-modal templatess",
      centered: true
    });
    this.Template_New = new Template();
    this.Template_Newbody = new Template();
    modalRef.componentInstance.template = this.Template_Newbody;
    modalRef.componentInstance.templatelist_length = this.template_list.length;
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      this.Template_Newbody = receivedEntry;
      this.dashboardApiService
        .createNewTemplate(this.Template_Newbody)
        .subscribe(
          res => {
            if (res.statusCode == 200) {
              this.Template_New = res.returnBody;
              this.template_list.push(this.Template_New);
              this.template_list = [...this.template_list];
              this.notificationService.success("SUCCESSFULLY_CREARED");
            } else {
              this.notificationService.error("FAILED_CREARED");
            }
            modalRef.close();
          },
          err => {
            this.notificationService.error(err);
            modalRef.close();
          }
        );
    });
  }

  onActivate(event) {
    const emitType = event.type;
    if (emitType == "dblclick") {
      let id = event.row.id;
      this.editTemplateModal(id);
    }

  }

  editTemplateModal(id) {
    const index = this.template_list.findIndex(t => t.id === id);
    const modalRef = this.modalService.open(EditTemplateModalComponent, {
      windowClass: "dl-md-modal templatess",
      centered: true
    });
    modalRef.componentInstance.edittemplate = this.template_list[index];
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      this.Template_New = receivedEntry;
      this.dashboardApiService
        .updateNewTemplate(this.Template_New)
        .subscribe(
          res => {
            if (res.statusCode == 200) {
              this.template_list[index] = this.Template_New;
              this.template_list = [...this.template_list];
              this.notificationService.success("SUCCESSFULLY_UPDATED");
            } else {
              this.notificationService.error("FAILED_UPDATED");
            }
            modalRef.close();
          },
          err => {
            this.notificationService.error(err);
            modalRef.close();
          }
        );
    })
  }

  deleteTemplateModel(id: number) {
    const index = this.template_list.findIndex(t => t.id === id);
    const modalRef = this.modalService.open(AlertComponent, {
      size: "sm",
      centered: true
    });
    // modalRef.componentInstance.dashboardDeteleModelShow = this.dashboardDeteleModelShow;
    modalRef.componentInstance.message = "ARE_YOU_SURE_DELETE";
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      // Delete database
      this.dashboardApiService.DeleteTemplate(id).subscribe(
        res => {
          if (JSON.stringify(res).length <= 2) {
            this.template_list.splice(index, 1);
            this.template_list = [...this.template_list];
            this.notificationService.success("SUCCESSFULLY_DELETED");
          } else {
            this.notificationService.error("FAILED_DELETED");
          }

          modalRef.close();
        },
        err => {
          this.notificationService.error(err);
          modalRef.close();
        }
      );
    });

  }

  deployTemplate(id: number) {
    const index = this.template_list.findIndex(t => t.id === id);
    const body = this.template_list[index];
    this.spinner.show();
    this.dashboardApiService.deployTemplateKibana(id, body).subscribe(
      res => {
        this.spinner.hide();
        if (JSON.stringify(res).length <= 2) {
          this.notificationService.success("Deploy_SUCCESSFULLY");
        } else {
          this.notificationService.error("Deploy_FAILED");
        }
      },
      err => {
        this.spinner.hide();
        this.notificationService.error(err);
      }
    );
  }

  updateFilter(searchValue) {
    const val = searchValue.toLowerCase();
    // filter our data
    const temps = this.temps.filter(function (d) {
      return d.name.toLowerCase().indexOf(val) !== -1 || !val;
    });
    // update the rows
    this.template_list = temps;
  }


}
