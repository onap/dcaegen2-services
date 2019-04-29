import { Component, OnInit } from "@angular/core";
import { HeaderService } from "../core/services/header.service";

@Component({
  selector: "app-topics",
  templateUrl: "./topics.component.html",
  styleUrls: ["./topics.component.css"]
})
export class TopicsComponent implements OnInit {
  constructor(private headerService: HeaderService) {}

  ngOnInit() {
    // Set page title
    this.headerService.setTitle("SIDEBAR.TOPICS");
  }
}
