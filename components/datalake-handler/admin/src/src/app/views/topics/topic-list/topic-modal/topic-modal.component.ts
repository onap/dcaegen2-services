/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2019 - 2020 QCT
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

import { Component, OnInit, Input, ViewChild } from "@angular/core";

import { NgbActiveModal, NgbTypeahead } from "@ng-bootstrap/ng-bootstrap";
import { RestApiService } from "src/app/core/services/rest-api.service";
import { AdminService } from "src/app/core/services/admin.service";
import { Topic } from "src/app/core/models/topic.model";
import {
  debounceTime,
  distinctUntilChanged,
  filter,
  map,
  mergeMap
} from "rxjs/operators";
import { from, Subject, Observable, merge } from "rxjs";
import { Kafka } from "src/app/core/models/kafka.model";
import { Db } from "src/app/core/models/db.model";

@Component({
  selector: "app-topic-modal",
  templateUrl: "./topic-modal.component.html",
  styleUrls: ["./topic-modal.component.css"]
})
export class TopicModalComponent implements OnInit {
  @Input() data: Topic;
  @Input() mode: string;
  @Input() selectedIndex: number;

  dataFormats: Array<string> = ["JSON", "XML"];
  idExFields: Array<any> = [];
  idExNewField: any = {};

  kafkas: Array<Kafka> = [];
  dbs: Array<Db> = [];
  dbTypeIds: Array<string> = [];

  // Autocomplete input
  @ViewChild("instance") instance: NgbTypeahead;
  focus$ = new Subject<string>();
  click$ = new Subject<string>();
  newTopicList: Array<string>;

  search = (text$: Observable<string>) => {
    const debouncedText$ = text$.pipe(
      debounceTime(200),
      distinctUntilChanged()
    );
    const clicksWithClosedPopup$ = this.click$.pipe(
      filter(() => !this.instance.isPopupOpen())
    );
    const inputFocus$ = this.focus$;

    return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
      map(term =>
        (term === ""
          ? this.newTopicList
          : this.newTopicList.filter(
              v => v.toLowerCase().indexOf(term.toLowerCase()) > -1
            )
        ).slice(0, 10)
      )
    );
  };

  constructor(
    public activeModal: NgbActiveModal,
    public adminService: AdminService,
    private restApiService: RestApiService
  ) {}

  ngOnInit() {
    // Get ID extration field
    this.idExFields = [];
    if (this.data.messageIdPath != null) {
      let feed = this.data.messageIdPath.split(",");
      for (let i = 0; i < feed.length; i++) {
        let data = { item: feed[i] };
        this.idExFields.push(data);
      }
    } else {
      this.idExFields.push([]);
    }

    // Init data
    this.initData();
  }

  initData() {
    this.getKafkas();
    this.getDbs();

    if (this.mode === "new") {
      this.getNewTopicList();
    }
  }

  getKafkas() {
    const get_kafkas = this.restApiService.getAllKafka().pipe(
      mergeMap(ks => from(ks)),
      map(k => {
        if (
          this.data.kafkas &&
          this.data.kafkas.toString().includes(k.id.toString())
        ) {
          k.checkedToSave = true;
        } else {
          k.checkedToSave = false;
        }
        this.kafkas.push(k);
      })
    );

    get_kafkas.subscribe();
  }

  getDbs() {
    const get_dbs = this.restApiService.getAllDbs().pipe(
      mergeMap(dbs => from(dbs)),
      map(db => {
        if (!this.dbTypeIds.includes(db.dbTypeId)) {
          this.dbTypeIds.push(db.dbTypeId);
        }
        if (
          this.data.sinkdbs &&
          this.data.sinkdbs.toString().includes(db.id.toString())
        ) {
          db.checkedToSave = true;
        } else {
          db.checkedToSave = false;
        }
        this.dbs.push(db);
      })
    );

    get_dbs.subscribe();
  }

  getNewTopicList() {
    const get_topicName = this.restApiService.getTopicNames().pipe(
      map(names => {
        this.newTopicList = names;
      })
    );

    get_topicName.subscribe();
  }

  onChabgeSelKafka(checked: boolean, id: string | number) {
    // Array initialize
    if (!this.data.kafkas) this.data.kafkas = [];

    if (checked) {
      // Add kafka_id into topic.kafkas
      if (
        this.data.kafkas &&
        !this.data.kafkas.toString().includes(id.toString())
      ) {
        this.data.kafkas.push(id.toString());
      }
    } else {
      // Remove kafka_id from topic.kafkas
      if (
        this.data.kafkas &&
        this.data.kafkas.toString().includes(id.toString())
      ) {
        this.data.kafkas.forEach((k_id, index) => {
          if (k_id.toString() === id.toString()) {
            this.data.kafkas.splice(index, 1);
            return;
          }
        });
      }
    }
  }

  onChabgeSelDb(checked: boolean, id: string | number) {
    // Array initialize
    if (!this.data.sinkdbs) this.data.sinkdbs = [];

    if (checked) {
      // Add db_id into topic.sinkdbs
      if (
        this.data.sinkdbs &&
        !this.data.sinkdbs.toString().includes(id.toString())
      ) {
        this.data.sinkdbs.push(id.toString());
      }
    } else {
      // Remove db_id from "topic.sinkdbs"
      if (
        this.data.sinkdbs &&
        this.data.sinkdbs.toString().includes(id.toString())
      ) {
        this.data.sinkdbs.forEach((db_id, index) => {
          if (db_id.toString() === id.toString()) {
            this.data.sinkdbs.splice(index, 1);
            return;
          }
        });
      }
    }
  }

  onClickAddIdField() {
    this.idExFields.push(this.idExNewField);
    this.idExNewField = {};
    this.onChangeSaveIdField();
  }

  onClickDelIdField(index: number) {
    if (this.idExFields.length > 1) {
      this.idExFields.splice(index, 1);
      this.onChangeSaveIdField();
    }
  }

  onChangeSaveIdField() {
    this.data.messageIdPath = "";

    for (let i = 0; i < this.idExFields.length; i++) {
      if (this.idExFields[i].item) {
        if (this.data.messageIdPath == "") {
          this.data.messageIdPath = this.idExFields[i].item;
        } else {
          this.data.messageIdPath += "," + this.idExFields[i].item;
        }
      }
    }
  }

  onClickMatTab(index: number) {
    this.selectedIndex = index;
  }

  isAddingMode() {
    let flag: boolean = false;

    if (this.mode === "new") flag = true;

    return flag;
  }
}
