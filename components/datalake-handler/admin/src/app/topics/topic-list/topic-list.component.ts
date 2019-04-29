import { Component, ViewChild } from "@angular/core";
import { RestApiService } from "../../core/services/rest-api.service";
import { Topic } from "../../core/models/Topic/topic.model";
import { NgbModal, ModalDismissReasons } from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: "app-topic-list",
  templateUrl: "./topic-list.component.html",
  styleUrls: ["./topic-list.component.css"]
})
export class TopicListComponent {
  // fake data
  topicList: Array<string> = [
    "AAI-EVENT1",
    "AAI-EVENT2",
    "AAI-EVENT3",
    "AAI-EVENT4",
    "AAI-EVENT5",
    "AAI-EVENT6",
    "AAI-EVENT7",
    "AAI-EVENT8",
    "AAI-EVENT9",
    "AAI-EVENT10",
    "AAI-EVENT11",
    "AAI-EVENT12",
    "AAI-EVENT13",
    "AAI-EVENT14",
    "AAI-EVENT15",
    "AAI-EVENT16",
    "AAI-EVENT17",
    "AAI-EVENT18",
    "AAI-EVENT19",
    "AAI-EVENT21",
    "AAI-EVENT22",
    "AAI-EVENT23",
    "AAI-EVENT24",
    "AAI-EVENT25",
    "AAI-EVENT26",
    "AAI-EVENT27",
    "AAI-EVENT28",
    "AAI-EVENT29",
    "__consumer_offsets",
    "msgrtr.apinode.metrics.dmaap",
    "unauthenticated.DCAE_CL_OUTPUT",
    "unauthenticated.SEC_FAULT_OUTPUT"
  ];
  topics: Topic[] = [];
  temp: Topic[] = []; // cache for topics
  topicDetail: Topic = new Topic();
  closeResult: string;
  dataFormats: Array<string> = ["JSON", "XML"];
  idExFields: Array<any> = [];
  idExNewField: any = {};

  loadingIndicator: boolean = true;

  constructor(
    private restApiService: RestApiService,
    private modalService: NgbModal
  ) {
    setTimeout(() => {
      this.loadingIndicator = false;
    }, 5000);
  }

  ngOnInit() {
    // fake data
    var topicCount: number = this.topicList.length;
    console.log("topic length:" + topicCount);
    for (var i = 0; i < topicCount; i++) {
      if (i % 2 == 0) {
        var feed = {
          name: this.topicList[i],
          login: "logintest" + i,
          pass: "passtest" + i,
          dbs: ["_DL_DEFAULT_", "couchbasedb", "druid", "mongodb"],
          enable: true,
          save_raw: true,
          data_format: "XML",
          ttl: 100 + i,
          correlate_cleared_message: true,
          message_id_path: "/event_id/id,/event_id/name",
          type: ""
        };
      } else {
        var feed = {
          name: this.topicList[i],
          login: "",
          pass: "",
          dbs: ["druid", "elasticsearch"],
          enable: false,
          save_raw: false,
          data_format: "XML",
          ttl: 100 + i,
          correlate_cleared_message: false,
          message_id_path: "/event_id/id,/event_id/name/event_id/ekko_name",
          type: ""
        };
      }

      if (feed.dbs.toString().search("_DL_DEFAULT")) {
        feed.type = "Unconfigured";
      } else {
        feed.type = "Configured";
      }
      this.topics.push(feed);
    }

    // for cache of datatable
    this.temp = this.topics;
  }

  updateDetail(name: string) {
    // clean
    this.topicDetail = null;
    this.idExFields = [];

    const index = this.topics.findIndex(topic => topic.name === name);

    this.topicDetail = {
      name: this.topics[index].name,
      login: this.topics[index].login,
      pass: this.topics[index].pass,
      dbs: this.topics[index].dbs,
      enable: this.topics[index].enable,
      save_raw: this.topics[index].save_raw,
      data_format: this.topics[index].data_format,
      ttl: this.topics[index].ttl,
      correlate_cleared_message: this.topics[index].correlate_cleared_message,
      message_id_path: this.topics[index].message_id_path,
      type: this.topics[index].type
    };

    var feed = this.topicDetail.message_id_path.split(",");
    for (var i = 0; i < feed.length; i++) {
      var data = { item: feed[i] };
      this.idExFields.push(data);
    }
  }

  addIdField() {
    this.idExFields.push(this.idExNewField);
    this.idExNewField = {};
  }

  deleteIdField(index: number) {
    if (this.idExFields.length > 1) {
      this.idExFields.splice(index, 1);
    }
  }

  saveTopic() {
    console.log("save topic");
    console.log("name: " + this.topicDetail.name);
    console.log("login: " + this.topicDetail.login);
    console.log("pass: " + this.topicDetail.pass);
    console.log("dbs: ");
    console.log("enable: " + this.topicDetail.enable);
    console.log("save_raw: " + this.topicDetail.save_raw);
    console.log("data_format: " + this.topicDetail.data_format);
    console.log("ttl: " + this.topicDetail.ttl);
    console.log(
      "correlate_cleared_message: " + this.topicDetail.correlate_cleared_message
    );

    this.topicDetail.message_id_path = "";
    for (var i = 0; i < this.idExFields.length; i++) {
      if (i == 0) {
        this.topicDetail.message_id_path = this.idExFields[i].item;
      } else {
        this.topicDetail.message_id_path =
          this.topicDetail.message_id_path + "," + this.idExFields[i].item;
      }
    }
    console.log("message_id_path: ", this.topicDetail.message_id_path);
  }

  updateFilter(event) {
    const val = event.target.value.toLowerCase();
    console.log("filter val: " + val);
    // filter data
    const temp = this.temp.filter(function(d) {
      return d.name.toLowerCase().indexOf(val) !== -1 || !val;
    });

    // update the rows
    this.topics = temp;
  }
}
