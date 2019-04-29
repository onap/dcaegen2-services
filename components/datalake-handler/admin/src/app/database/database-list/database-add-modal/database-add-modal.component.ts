import { Component, OnInit, Input, ViewChild } from "@angular/core";
import { NgbModal, NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";

// DB modal components
import { CouchbaseComponent } from "../dbs-modal/couchbase/couchbase.component";
import { DruidComponent } from "../dbs-modal/druid/druid.component";
import { ElasticsearchComponent } from "../dbs-modal/elasticsearch/elasticsearch.component";
import { MongodbComponent } from "../dbs-modal/mongodb/mongodb.component";

@Component({
  selector: "app-database-add-modal",
  templateUrl: "./database-add-modal.component.html",
  styleUrls: ["./database-add-modal.component.css"]
})
export class DatabaseAddModalComponent implements OnInit {
  seletedDb: string;

  constructor(
    private modalService: NgbModal,
    public activeModal: NgbActiveModal
  ) {}

  ngOnInit() {}

  clickDb(name: any) {
    console.log("seleted: " + name);
    if (name != null) {
      this.seletedDb = name;
    }
  }

  openDbDetailModal() {
    this.activeModal.close();

    switch (this.seletedDb) {
      case "Couchbase": {
        const modalRef = this.modalService.open(CouchbaseComponent, {
          size: "lg",
          centered: true
        });
        modalRef.componentInstance.name = "World";
        break;
      }
      case "Druid": {
        const modalRef = this.modalService.open(DruidComponent, {
          size: "lg",
          centered: true
        });
        modalRef.componentInstance.name = "World";
        break;
      }
      case "Elasticsearch": {
        const modalRef = this.modalService.open(ElasticsearchComponent, {
          size: "lg",
          centered: true
        });
        modalRef.componentInstance.name = "World";
        break;
      }
      case "MongoDB": {
        const modalRef = this.modalService.open(MongodbComponent, {
          size: "lg",
          centered: true
        });
        modalRef.componentInstance.name = "World";
        break;
      }
      default: {
        break;
      }
    }
  }
}
