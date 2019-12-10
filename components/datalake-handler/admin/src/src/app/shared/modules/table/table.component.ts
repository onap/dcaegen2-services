import { Component, OnInit, Input, Output, EventEmitter } from "@angular/core";

/**
 * @contributor Chunmeng Guo
 */

@Component({
  selector: "app-table",
  templateUrl: "./table.component.html",
  styleUrls: ["./table.component.css"]
})
export class TableComponent implements OnInit {
  @Input() columns: Array<any> = [];
  @Input() data: Array<any> = [];
  @Output() btnTableAction = new EventEmitter<object>();
  loadingIndicator: boolean = true;
  template_list: Array<any> = [];

  mesgNoData = {
    emptyMessage: `
      <div class="d-flex justify-content-center">
        <div class="p-2">
          <label class="dl-nodata">No data</label>
        </div>
      </div>
    `
  };

  constructor() {}

  ngOnInit() {
    setTimeout(() => {
      this.loadingIndicator = false;
    }, 500);
  }

  tableAction(event: any, actionId: number) {
    console.log("action id: " + actionId);
    console.log("edit: " + event.row.id);
    let passValueArr: Array<any> = [];
    passValueArr.push(event);
    passValueArr.push(actionId);
    this.btnTableAction.emit(passValueArr);
  }

  rowOnActivate(event: any) {
    const emitType = event.type;
    if (emitType == "dblclick") {
      console.log("Activate Event", event);
      let name = event.row.id;
      // this.openTopicModal(name);
      console.log("row name: " + name);
    }
  }
}
