import { Component, OnInit, Input } from "@angular/core";
import { NgbModal, NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: "app-mongodb",
  templateUrl: "./mongodb.component.html",
  styleUrls: ["./mongodb.component.css"]
})
export class MongodbComponent implements OnInit {
  @Input() title = "1234";

  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit() {}
}
