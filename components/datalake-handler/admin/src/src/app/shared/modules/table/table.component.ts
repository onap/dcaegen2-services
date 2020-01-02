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

  tableAction(action: string, id: number | string) {
    let passValueArr: Array<any> = [];
    passValueArr.push(action);
    passValueArr.push(id);

    // array[0] for action
    // array[1] for value
    console.log("tableAction");
    this.btnTableAction.emit(passValueArr);
  }

  rowOnActivate(event: any) {
    const emitType = event.type;

    if (emitType == "dblclick") {
      let passValueArr: Array<any> = [];
      passValueArr.push("edit");
      passValueArr.push(event.row.id);

      // // array[0] for action
      // // array[1] for value
      this.btnTableAction.emit(passValueArr);
    }
  }
}
