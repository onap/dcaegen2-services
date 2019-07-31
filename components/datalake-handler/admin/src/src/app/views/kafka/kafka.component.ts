import { Component, OnInit } from '@angular/core';
import { AdminService } from "../../core/services/admin.service";
@Component({
  selector: 'app-kafka',
  templateUrl: './kafka.component.html',
  styleUrls: ['./kafka.component.css']
})
export class KafkaComponent implements OnInit {
  constructor(private adminService: AdminService) {
    // Set page title
    this.adminService.setTitle("SIDEBAR.KAFKA");
  }

  ngOnInit() { }

}
