import { Component, OnInit } from "@angular/core";
import { HeaderService } from "../core/services/header.service";

@Component({
  selector: "app-database",
  templateUrl: "./database.component.html",
  styleUrls: ["./database.component.css"]
})
export class DatabaseComponent implements OnInit {
  constructor(private headerService: HeaderService) {}

  ngOnInit() {
    this.headerService.setTitle("SIDEBAR.DATABASE");
  }
}
