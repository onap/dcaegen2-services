import { Component, OnInit, ViewChild, ElementRef } from "@angular/core";
import { Db } from "../../core/models/DB/db.model";
import { NgbModal, ModalDismissReasons } from "@ng-bootstrap/ng-bootstrap";
import { DatabaseAddModalComponent } from "./database-add-modal/database-add-modal.component";

// DB modal components
import { CouchbaseComponent } from "./dbs-modal/couchbase/couchbase.component";
import { DruidComponent } from "./dbs-modal/druid/druid.component";
import { ElasticsearchComponent } from "./dbs-modal/elasticsearch/elasticsearch.component";
import { MongodbComponent } from "./dbs-modal/mongodb/mongodb.component";

@Component({
  selector: "app-database-list",
  templateUrl: "./database-list.component.html",
  styleUrls: ["./database-list.component.css"]
})
export class DatabaseListComponent implements OnInit {
  constructor(private modalService: NgbModal) {}

  @ViewChild("addDbModal") private addDBModal: ElementRef;

  dbs: Db[] = [];

  dbSupports: any[];

  ngOnInit() {
    // TODO: rest api
    this.dbs.push(
      {
        name: "Couchbase",
        host: "host1",
        login: "login1",
        pass: "pass1",
        port: 111,
        ssl: true
      },
      {
        name: "Druid",
        host: "host2",
        login: "login2",
        pass: "pass2",
        port: 222,
        ssl: true
      },
      {
        name: "Elasticsearch",
        host: "host3",
        login: "login3",
        pass: "pass3",
        port: 333,
        ssl: false
      },
      {
        name: "MongoDB",
        host: "host4",
        login: "login4",
        pass: "pass4",
        port: 444,
        ssl: false
      }
    );
  }

  openDbAddModal() {
    this.modalService.open(DatabaseAddModalComponent, {
      size: "lg",
      centered: true
    });
  }

  openDbDetailModal(db: Db) {
    console.log("db name: " + db.name);

    switch (db.name) {
      case "Couchbase": {
        const modalRef = this.modalService.open(CouchbaseComponent, {
          size: "lg",
          centered: true
        });
        modalRef.componentInstance.dbname = "Couchbase";
        modalRef.componentInstance.name = db.name;
        modalRef.componentInstance.host = db.host;
        modalRef.componentInstance.port = db.port;
        modalRef.componentInstance.login = db.login;
        modalRef.componentInstance.pass = db.pass;
        modalRef.componentInstance.ssl = db.ssl;
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
