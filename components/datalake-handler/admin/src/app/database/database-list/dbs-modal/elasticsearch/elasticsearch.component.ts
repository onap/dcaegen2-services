import { Component, OnInit } from "@angular/core";
import { NgbModal, NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: "app-elasticsearch",
  templateUrl: "./elasticsearch.component.html",
  styleUrls: ["./elasticsearch.component.css"]
})
export class ElasticsearchComponent implements OnInit {
  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit() {}
}
