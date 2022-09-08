import { Component, OnInit } from "@angular/core";
import { AdminService } from "../../core/services/admin.service";

@Component({
  selector: "app-topics",
  templateUrl: "./topics.component.html",
  styleUrls: ["./topics.component.css"]
})
export class TopicsComponent implements OnInit {
  constructor(private adminService: AdminService) {
    // Set page title
    this.adminService.setTitle("SIDEBAR.TOPICS");
  }

  ngOnInit() { }
}
