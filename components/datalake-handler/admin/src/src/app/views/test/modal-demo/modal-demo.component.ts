import { Component, OnInit, Input } from "@angular/core";

@Component({
  selector: "app-modal-demo",
  templateUrl: "./modal-demo.component.html",
  styleUrls: ["./modal-demo.component.css"]
})
export class ModalDemoComponent implements OnInit {
  @Input() data: any;

  constructor() {}

  ngOnInit() {}
}
