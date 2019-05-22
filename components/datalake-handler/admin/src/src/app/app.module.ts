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

import { HeaderComponent } from "./header/header.component";
import { SidebarComponent } from "./sidebar/sidebar.component";

import { FeederComponent } from "./feeder/feeder.component";
import { TopicsComponent } from "./topics/topics.component";
import { DatabaseComponent } from "./database/database.component";
import { TopicListComponent } from "./topics/topic-list/topic-list.component";

// Service
import { AdminService } from "./core/services/admin.service";
import { RestApiService } from "./core/services/rest-api.service";
import { ToastrNotificationService } from "src/app/core/services/toastr-notification.service";

// i18n
import { TranslateModule, TranslateLoader } from "@ngx-translate/core";
import { TranslateHttpLoader } from "@ngx-translate/http-loader";

export function createLoader(http: HttpClient) {
  return new TranslateHttpLoader(http);
}

// REST API
import { HttpClientModule } from "@angular/common/http";
import { HttpClient } from "@angular/common/http";

import { DatabaseListComponent } from "./database/database-list/database-list.component";
import { NgxDatatableModule } from "@swimlane/ngx-datatable";
import { CouchbaseComponent } from "./database/database-list/dbs-modal/couchbase/couchbase.component";
import { MongodbComponent } from "./database/database-list/dbs-modal/mongodb/mongodb.component";
import { DatabaseAddModalComponent } from "./database/database-list/database-add-modal/database-add-modal.component";
import { ElasticsearchComponent } from "./database/database-list/dbs-modal/elasticsearch/elasticsearch.component";
import { DruidComponent } from "./database/database-list/dbs-modal/druid/druid.component";

// Modals
import { TopicDetailModalComponent } from "./topics/topic-list/topic-detail-modal/topic-detail-modal.component";
import { TopicConfigModalComponent } from "./topics/topic-list/topic-config-modal/topic-config-modal.component";
import { ToastrNotificationComponent } from "./core/toastr-notification/toastr-notification.component";
import { AlertComponent } from "./core/alert/alert.component";
import { AboutComponent } from "./about/about.component";

// Loading spinner
import { NgxSpinnerModule } from "ngx-spinner";

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
    DatabaseAddModalComponent,
    ElasticsearchComponent,
    DruidComponent,
    TopicDetailModalComponent,
    TopicConfigModalComponent,
    ToastrNotificationComponent,
    AlertComponent,
    AboutComponent
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
    NgxSpinnerModule
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
    TopicDetailModalComponent,
    TopicConfigModalComponent
  ]
})
export class AppModule {}
