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
import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {DashboardApiService} from "src/app/core/services/dashboard-api.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Template,newTemplate} from "src/app/core/models/template.model";
// Loading spinner
import {NgxSpinnerService} from "ngx-spinner";

// modal
import {NewTemplateModalComponent} from "./new-template-modal/new-template-modal.component";
import {EditTemplateModalComponent} from "./edit-template-modal/edit-template-modal.component";
import {AlertComponent} from "src/app/core/alert/alert.component";
// notify
import {ToastrNotificationService} from "src/app/core/services/toastr-notification.service";

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
  Template_Newbody: newTemplate;
  dashboardDeteleModelShow = true;
  loadingIndicator: boolean = true;
  selectedLangs = sessionStorage.getItem("selectedLang");
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
    private dashboardApiService: DashboardApiService,
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
    this.Template_Newbody = new newTemplate();
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
        topic: data["topic"],
        designType: data["designType"],
      };
      t.push(feed);
    }
    return t;
  }

  newTemplateModal() {
    this.selectedLangs = sessionStorage.getItem("selectedLang");
    let tips = "";
    const modalRef = this.modalService.open(NewTemplateModalComponent, {
      windowClass: "dl-md-modal",
      centered: true
    });
    this.Template_New = new Template();
    this.Template_Newbody = new newTemplate();
    modalRef.componentInstance.template = this.Template_Newbody;
    modalRef.componentInstance.selectedLangs = this.selectedLangs;
    modalRef.componentInstance.templatelist_length = this.template_list.length;
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      this.Template_Newbody = receivedEntry;
      let name = this.Template_Newbody.name;
      this.dashboardApiService
        .createNewTemplate(this.Template_Newbody)
        .subscribe(
          res => {
            if (res.statusCode == 200) {
              this.Template_New = res.returnBody;
              console.log(this.Template_New,"this.Template_New add Success");
              this.template_list.push(this.Template_New);
              this.template_list = [...this.template_list];
              console.log(this.template_list, "this.template_list,inserted");
              if (this.selectedLangs == "en-us") {
                tips = "Success inserted."
              } else if (this.selectedLangs == "zh-hans") {
                tips = "新增成功。"
              } else if (this.selectedLangs == "zh-hant") {
                tips = "新增成功。"
              }
              this.notificationService.success('"' + name + '"' + tips);
            } else {
              if (this.selectedLangs == "en-us") {
                tips = "Fail inserted."
              } else if (this.selectedLangs == "zh-hans") {
                tips = "新增失败。"
              } else if (this.selectedLangs == "zh-hant") {
                tips = "新增失敗。"
              }
              this.notificationService.error('"' + name + '"' + tips);
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

  editTemplateModal(id: number) {
    this.selectedLangs = sessionStorage.getItem("selectedLang");
    let tips = "";
    const index = this.template_list.findIndex(t => t.id === id);
    // const name = this.template_list[index].name;
    const modalRef = this.modalService.open(EditTemplateModalComponent, {
      windowClass: "dl-md-modal",
      centered: true
    });
    modalRef.componentInstance.selectedLangs = this.selectedLangs;
    modalRef.componentInstance.edittemplate = this.template_list[index];
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      const name = receivedEntry.name;
      this.Template_New = receivedEntry;
      this.dashboardApiService
        .updateNewTemplate(this.Template_New)
        .subscribe(
          res => {

            if (res.statusCode == 200) {
              this.template_list[index] = this.Template_New;
              this.template_list = [...this.template_list];
              console.log(this.template_list, "this.template_list,update");
              if (this.selectedLangs == "en-us") {
                tips = "Success updated."
              } else if (this.selectedLangs == "zh-hans") {
                tips = "更新成功。"
              } else if (this.selectedLangs == "zh-hant") {
                tips = "更新成功。"
              }
              this.notificationService.success('"' + name + '"' + tips);

            } else {
              if (this.selectedLangs == "en-us") {
                tips = "Fail updated."
              } else if (this.selectedLangs == "zh-hans") {
                tips = "更新失败。"
              } else if (this.selectedLangs == "zh-hant") {
                tips = "更新失敗。"
              }
              this.notificationService.error('"' + name + '"' + tips);
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
    this.selectedLangs = sessionStorage.getItem("selectedLang");
    let tips = "";
    if (this.selectedLangs == "en-us") {
      tips = "Are you sure you want to delete ";
    } else if (this.selectedLangs == "zh-hans") {
      tips = "您确定您要删除";
    } else if (this.selectedLangs == "zh-hant") {
      tips = "您確定您要刪除";
    }
    const index = this.template_list.findIndex(t => t.id === id);
    const modalRef = this.modalService.open(AlertComponent, {
      size: "sm",
      centered: true
    });
    const name = this.template_list[index].name;
    // modalRef.componentInstance.dashboardDeteleModelShow = this.dashboardDeteleModelShow;
    modalRef.componentInstance.message =
      tips + '"' + name + '"' + ' ?';
    modalRef.componentInstance.passEntry.subscribe(receivedEntry => {
      // Delete database
      this.dashboardApiService.DeleteTemplate(id).subscribe(
        res => {
          console.log(res,"template detele res");
          console.log(JSON.stringify(res).length,"JSON.stringify(res).length");
          if (JSON.stringify(res).length<=2) {
            console.log("Success deleted template");
            this.template_list.splice(index, 1);
            this.template_list = [...this.template_list];
            if (this.selectedLangs == "en-us") {
              tips = "Success deleted."
            } else if (this.selectedLangs == "zh-hans") {
              tips = "删除成功。"
            } else if (this.selectedLangs == "zh-hant") {
              tips = "刪除成功。"
            }
            this.notificationService.success('"' + name + '"' + tips);
          } else {
            console.log("Fail deleted template");
            if (this.selectedLangs == "en-us") {
              tips = "Fail deleted."
            } else if (this.selectedLangs == "zh-hans") {
              tips = "删除失败。"
            } else if (this.selectedLangs == "zh-hant") {
              tips = "刪除失敗。"
            }
            this.notificationService.error('"' + name + '"' + tips);
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
    this.selectedLangs = sessionStorage.getItem("selectedLang");
    let tips = "";
    const index = this.template_list.findIndex(t => t.id === id);
    const name = this.template_list[index].name;
    const body = this.template_list[index];
    this.spinner.show();
    // this.dashboardApiService.deployTemplate(id).subscribe(
    //   res => {
    //     console.log(res,"reshost");
    //     const host = res.source.host,
    //           port = res.source.port;
    //     if (res.status == "success") {
    //       console.log(res)

    this.dashboardApiService.deployTemplateKibana(id, body).subscribe(
      res => {
        this.spinner.hide();
        console.log(res,"template deploy res");
        console.log(JSON.stringify(res).length,"JSON.stringify(res).length");
        if (JSON.stringify(res).length<=2) {
          if (this.selectedLangs == "en-us") {
            tips = "Success deploy."
          } else if (this.selectedLangs == "zh-hans") {
            tips = "部署成功。"
          } else if (this.selectedLangs == "zh-hant") {
            tips = "部署成功。"
          }
          this.notificationService.success('"' + name + '"' + tips);
        } else {
          if (this.selectedLangs == "en-us") {
            tips = "Fail deleted."
          } else if (this.selectedLangs == "zh-hans") {
            tips = "部署失败。"
          } else if (this.selectedLangs == "zh-hant") {
            tips = "部署失敗。"
          }
          this.notificationService.error('"' + name + '"' + tips);
        }
      },
      err => {
        this.spinner.hide();
        this.notificationService.error(err);
      }
    );
    //     } else {
    //       this.spinner.hide();
    //       this.notificationService.error( name + " Fail deploy.");
    //     }
    //   },
    //   err => {
    //     this.spinner.hide();
    //     this.notificationService.error(err);
    //   }
    // );

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
