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

import { BrowserModule } from "@angular/platform-browser";
import { NgModule } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { NgbModule } from "@ng-bootstrap/ng-bootstrap";

import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app.component";

import { HeaderComponent } from "./shared/layout/header/header.component";
import { SidebarComponent } from "./shared/layout/sidebar/sidebar.component";

import { FeederComponent } from "./views/feeder/feeder.component";
import { TopicsComponent } from "./views/topics/topics.component";
import { DatabaseComponent } from "./views/database/database.component";
import { TopicListComponent } from "./views/topics/topic-list/topic-list.component";

// Service
import { AdminService } from "./core/services/admin.service";
import { RestApiService } from "./core/services/rest-api.service";
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";

// i18n
import { TranslateModule, TranslateLoader } from "@ngx-translate/core";
import { TranslateHttpLoader } from "@ngx-translate/http-loader";

export function createLoader(http: HttpClient) {
  return new TranslateHttpLoader(http);
}

// REST API
import { HttpClientModule } from "@angular/common/http";
import { HttpClient } from "@angular/common/http";

import { DatabaseListComponent } from "./views/database/database-list/database-list.component";
import { NgxDatatableModule } from "@swimlane/ngx-datatable";
import { CouchbaseComponent } from "./views/database/database-list/dbs-modal/couchbase/couchbase.component";
import { MongodbComponent } from "./views/database/database-list/dbs-modal/mongodb/mongodb.component";
import { HdfsComponent } from "./views/database/database-list/dbs-modal/hdfs/hdfs.component";
import { DatabaseAddModalComponent } from "./views/database/database-list/database-add-modal/database-add-modal.component";
import { ElasticsearchComponent } from "./views/database/database-list/dbs-modal/elasticsearch/elasticsearch.component";
import { DruidComponent } from "./views/database/database-list/dbs-modal/druid/druid.component";

// Modals
import { TopicDetailModalComponent } from "./views/topics/topic-list/topic-detail-modal/topic-detail-modal.component";
import { TopicConfigModalComponent } from "./views/topics/topic-list/topic-config-modal/topic-config-modal.component";
import { ToastrNotificationComponent } from "./shared/components/toastr-notification/toastr-notification.component";
import { AlertComponent } from "./shared/components/alert/alert.component";
import { AboutComponent } from "./views/about/about.component";

// Loading spinner
import { NgxSpinnerModule } from "ngx-spinner";
import { DashboardSettingComponent } from "./views/dashboard-setting/dashboard-setting.component";
import { DashboardListComponent } from "./views/dashboard-setting/dashboard-list/dashboard-list.component";
import { TemplateComponent } from "./views/dashboard-setting/template/template.component";
import { CreateDashboardComponent } from "./views/dashboard-setting/dashboard-list/create-dashboard/create-dashboard.component";
import { TemplateListComponent } from "./views/dashboard-setting/template/template-list/template-list.component";
import { NewTemplateModalComponent } from "./views/dashboard-setting/template/template-list/new-template-modal/new-template-modal.component";
import { EditTemplateModalComponent } from "./views/dashboard-setting/template/template-list/edit-template-modal/edit-template-modal.component";
import { NewTopicModelComponent } from "./views/topics/topic-list/new-topic-model/new-topic-model.component";
import { TestComponent } from "./views/test/test.component";
import { ModalComponent } from "./shared/modules/modal/modal.component";
import { TableComponent } from "./shared/modules/table/table.component";
import { SearchComponent } from "./shared/modules/search/search.component";
import { CardComponent } from "./shared/modules/card/card.component";
import { ButtonComponent } from "./shared/components/Button/button.component";
import { ModalDirective } from "./shared/modules/modal/modal.directive";
import { ModalDemoComponent } from "./views/test/modal-demo/modal-demo.component";
import { KafkaComponent } from './views/kafka/kafka.component';
// Angular SVG Icon
import { AngularSvgIconModule } from "angular-svg-icon";
import { KafkaListComponent } from './views/kafka/kafka-list/kafka-list.component';
import { NewKafkaModalComponent } from './views/kafka/kafka-list/new-kafka-modal/new-kafka-modal.component';
import { EditKafkaModalComponent } from './views/kafka/kafka-list/edit-kafka-modal/edit-kafka-modal.component';


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
    CouchbaseComponent,
    MongodbComponent,
    HdfsComponent,
    DatabaseAddModalComponent,
    ElasticsearchComponent,
    DruidComponent,
    TopicDetailModalComponent,
    TopicConfigModalComponent,
    ToastrNotificationComponent,
    AlertComponent,
    AboutComponent,
    DashboardSettingComponent,
    DashboardListComponent,
    CreateDashboardComponent,
    TemplateComponent,
    TemplateListComponent,
    NewTemplateModalComponent,
    EditTemplateModalComponent,
    NewTopicModelComponent,
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
    NewKafkaModalComponent,
    EditKafkaModalComponent
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
    AngularSvgIconModule
  ],
  providers: [AdminService, RestApiService, ToastrNotificationService],
  bootstrap: [AppComponent],
  entryComponents: [
    AlertComponent,
    DatabaseAddModalComponent,
    CouchbaseComponent,
    DruidComponent,
    ElasticsearchComponent,
    MongodbComponent,
    HdfsComponent,
    TopicDetailModalComponent,
    TopicConfigModalComponent,
    NewTopicModelComponent,
    CreateDashboardComponent,
    NewTemplateModalComponent,
    EditTemplateModalComponent,
    ModalComponent,
    ModalDemoComponent,
    KafkaComponent,
    NewKafkaModalComponent,
    EditKafkaModalComponent
  ]
})
export class AppModule {}
