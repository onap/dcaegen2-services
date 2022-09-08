/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2019 - 2020 QCT
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

import { BrowserModule } from "@angular/platform-browser";
import { NgModule } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { NgbModule } from "@ng-bootstrap/ng-bootstrap";
import { TranslateModule, TranslateLoader } from "@ngx-translate/core";
import { TranslateHttpLoader } from "@ngx-translate/http-loader";
import { MatTabsModule } from "@angular/material";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { HttpClientModule } from "@angular/common/http";
import { HttpClient } from "@angular/common/http";
export function createLoader(http: HttpClient) {
  return new TranslateHttpLoader(http);
}
import { NgxDatatableModule } from "@swimlane/ngx-datatable";

import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app.component";

// DataLake pages
import { FeederComponent } from "./views/feeder/feeder.component";
import { TopicsComponent } from "./views/topics/topics.component";
import { TopicListComponent } from "./views/topics/topic-list/topic-list.component";
import { DatabaseComponent } from "./views/database/database.component";
import { DatabaseListComponent } from "./views/database/database-list/database-list.component";
import { KafkaComponent } from "./views/kafka/kafka.component";
import { KafkaListComponent } from "./views/kafka/kafka-list/kafka-list.component";
import { ToolsComponent } from "./views/tools/tools.component";
import { DashboardSettingComponent } from "./views/dashboard-setting/dashboard-setting.component";
import { DashboardListComponent } from "./views/dashboard-setting/dashboard-list/dashboard-list.component";
import { CreateDashboardComponent } from "./views/dashboard-setting/dashboard-list/create-dashboard/create-dashboard.component";
import { TemplateComponent } from "./views/dashboard-setting/template/template.component";
import { TemplateListComponent } from "./views/dashboard-setting/template/template-list/template-list.component";
import { AboutComponent } from "./views/about/about.component";
import { TestComponent } from "./views/test/test.component";

// Services
import { AdminService } from "./core/services/admin.service";
import { RestApiService } from "./core/services/rest-api.service";
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";

// Modals
import { ModalDemoComponent } from "./views/test/modal-demo/modal-demo.component";
import { TopicModalComponent } from "./views/topics/topic-list/topic-modal/topic-modal.component";
import { KafkaModalComponent } from "./views/kafka/kafka-list/kafka-modal/kafka-modal.component";
import { DbModalComponent } from "./views/database/database-list/db-modal/db-modal.component";
import { ToolModalComponent } from "./views/tools/tool-modal/tool-modal.component";
import { TemplateModalComponent } from "./views/dashboard-setting/template/template-list/template-modal/template-modal.component";

// Shared modules
import { HeaderComponent } from "./shared/layout/header/header.component";
import { SidebarComponent } from "./shared/layout/sidebar/sidebar.component";
import { TableComponent } from "./shared/modules/table/table.component";
import { SearchComponent } from "./shared/modules/search/search.component";
import { CardComponent } from "./shared/modules/card/card.component";
import { ButtonComponent } from "./shared/components/Button/button.component";
import { AngularSvgIconModule } from "angular-svg-icon";
import { IconComponent } from "./shared/components/icon/icon.component";
import { NgxSpinnerModule } from "ngx-spinner";
import { ModalComponent } from "./shared/modules/modal/modal.component";
import { ModalDirective } from "./shared/modules/modal/modal.directive";
import { ToastrNotificationComponent } from "./shared/components/toastr-notification/toastr-notification.component";
import { AlertComponent } from "./shared/components/alert/alert.component";

// Others

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    SidebarComponent,
    FeederComponent,
    TopicsComponent,
    DatabaseComponent,
    TopicListComponent,
    DatabaseListComponent,
    ToastrNotificationComponent,
    AlertComponent,
    AboutComponent,
    DashboardSettingComponent,
    DashboardListComponent,
    CreateDashboardComponent,
    TemplateComponent,
    TemplateListComponent,
    TestComponent,
    ModalComponent,
    TableComponent,
    SearchComponent,
    CardComponent,
    ButtonComponent,
    ModalDirective,
    ModalDemoComponent,
    KafkaComponent,
    KafkaListComponent,
    ToolsComponent,
    IconComponent,
    TopicModalComponent,
    KafkaModalComponent,
    DbModalComponent,
    ToolModalComponent,
    TemplateModalComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    NgbModule,
    HttpClientModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: createLoader,
        deps: [HttpClient]
      }
    }),
    FormsModule,
    NgxDatatableModule,
    NgxSpinnerModule,
    AngularSvgIconModule,
    MatTabsModule,
    BrowserAnimationsModule
  ],
  providers: [AdminService, RestApiService, ToastrNotificationService],
  bootstrap: [AppComponent],
  entryComponents: [
    AlertComponent,
    CreateDashboardComponent,
    ModalComponent,
    ModalDemoComponent,
    KafkaComponent,
    TopicModalComponent,
    KafkaModalComponent,
    DbModalComponent,
    ToolModalComponent,
    TemplateModalComponent
  ]
})
export class AppModule {}
