import { Component, OnInit, Input } from "@angular/core";
import { NgbModal, NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: "app-couchbase",
  templateUrl: "./couchbase.component.html",
  styleUrls: ["./couchbase.component.css"]
})
export class CouchbaseComponent implements OnInit {
  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit() {}
}
