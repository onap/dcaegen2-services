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
