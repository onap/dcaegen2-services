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

import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";

import { FeederComponent } from "./feeder/feeder.component";
import { TopicsComponent } from "./topics/topics.component";
import { DatabaseComponent } from "./database/database.component";

const routes: Routes = [
  { path: "", redirectTo: "/feeder", pathMatch: "full" },
  { path: "feeder", component: FeederComponent },
  { path: "topics", component: TopicsComponent },
  { path: "database", component: DatabaseComponent }
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
