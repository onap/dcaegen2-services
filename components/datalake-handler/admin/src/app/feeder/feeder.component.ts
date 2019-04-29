import { Component, OnInit } from "@angular/core";
import { HeaderService } from "../core/services/header.service";

@Component({
  selector: "app-feeder",
  templateUrl: "./feeder.component.html",
  styleUrls: ["./feeder.component.css"]
})
export class FeederComponent implements OnInit {
  constructor(private headerService: HeaderService) {}

  topicNumber: number = 11;
  topicSuccess: number = 9;
  topicFail: number = 1;
  topicEvents: number = 50;

  ngOnInit() {
    this.headerService.setTitle("SIDEBAR.FEDDFER");
  }
}
