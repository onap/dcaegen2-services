import { Component, OnInit } from '@angular/core';
import { ToastrNotificationService } from "src/app/shared/components/toastr-notification/toastr-notification.service";
@Component({
  selector: 'app-test',
  templateUrl: './test.component.html',
  styleUrls: ['./test.component.css']
})
export class TestComponent implements OnInit {

  modalShow: boolean = false;
  mockcolumns: Array<any> = [];
  mocktabledata: Array<any> = [];

  constructor(private notificationService: ToastrNotificationService, ) { }

  ngOnInit() {
    this.mockcolumns = [
      { name: 'TEMPLATE_NAME', width: '100', dataIndex: 'name', sortable: true },
      { name: 'TEMPLATE_TYPE', width: '180', dataIndex: 'type' },
      { name: 'TOPICS_NAME', width: '220', dataIndex: 'topic', renderText: '3' },
      { name: 'DEPLOY_TO_DASHBOARD', width: '220', dataIndex: '', icontext: 'DEPLOY' },
      { name: '', width: '20', dataIndex: '', icon: 'trash' },
    ]
    this.mocktabledata = [{
      name: 'aaaa', type: '333', topic: '尽快尽快'
    },
    {
      name: 'ccccc', type: '666', topic: '2222'
    }, {
      name: 'bbbbb', type: '77777', topic: '555'
    }]
  }
  buttonAction(string: string = '') {
    switch (string) {
      case 'modal':
        this.modalShow = true;
        break;
      case 'modalcancel':
        this.modalShow = false;
        break;
      default:
        this.notificationService.success(string + " action successful!");
        break;
    }
  }
}
