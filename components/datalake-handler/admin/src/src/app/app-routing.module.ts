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

import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";

//test components for module testing
import { TestComponent } from "./views/test/test.component";

import { FeederComponent } from "./views/feeder/feeder.component";
import { KafkaComponent } from "./views/kafka/kafka.component";
import { TopicsComponent } from "./views/topics/topics.component";
import { DatabaseComponent } from "./views/database/database.component";
import { AboutComponent } from "./views/about/about.component";
import { TemplateComponent } from "./views/dashboard-setting/template/template.component";
import { ToolsComponent } from "./views/tools/tools.component";
import { DataExposureComponent } from "./views/data-exposure/data-exposure.component";

const routes: Routes = [
  { path: "", redirectTo: "/feeder", pathMatch: "full" },
  { path: "test", component: TestComponent },
  { path: "feeder", component: FeederComponent },
  { path: "kafka", component: KafkaComponent },
  { path: "topics", component: TopicsComponent },
  { path: "database", component: DatabaseComponent },
  { path: "about", component: AboutComponent },
  { path: "tools", component: ToolsComponent },
  { path: "dashboard-setting/template", component: TemplateComponent },
  { path: "data-exposure", component: DataExposureComponent }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      useHash: true
    })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {}
