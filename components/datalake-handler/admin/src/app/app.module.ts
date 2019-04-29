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
import { HeaderService } from "./core/services/header.service";
import { RestApiService } from "./core/services/rest-api.service";

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
    DruidComponent
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
    NgxDatatableModule
  ],
  providers: [HeaderService, RestApiService],
  bootstrap: [AppComponent],
  entryComponents: [
    DatabaseAddModalComponent,
    CouchbaseComponent,
    DruidComponent,
    ElasticsearchComponent,
    MongodbComponent
  ]
})
export class AppModule {}
