/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2019 QCT
 *=================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

/**
 *
 * @author Ekko Chang
 *
 */

import { Component, Output, EventEmitter } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: "app-database-add-modal",
  templateUrl: "./database-add-modal.component.html",
  styleUrls: ["./database-add-modal.component.css"]
})
export class DatabaseAddModalComponent {
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  seletedItem: string = "";

  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit() {}

  clickItem(name: string) {
    this.seletedItem = name;
  }

  passBack() {
    this.passEntry.emit(this.seletedItem);
  }
}
