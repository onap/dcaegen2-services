import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit {
  @Input() columns: Array<any> = [];
  @Input() data: Array<any> = [];

  loadingIndicator: boolean = false;
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

  constructor() { }

  ngOnInit() {

  }
}
