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

import {
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter,
  ViewChild,
  ComponentFactoryResolver
} from "@angular/core";

import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { ModalDirective } from "src/app/shared/modules/modal/modal.directive";
import { ModalContentData } from "src/app/shared/modules/modal/modal.data";
import { ModalInterface } from "src/app/shared/modules/modal/modal.interface";

@Component({
  selector: "app-modal",
  templateUrl: "./modal.component.html",
  styleUrls: ["./modal.component.css"]
})
export class ModalComponent implements OnInit {
  @Input() title: string;
  @Input() notice: string;
  @Input() mode: string;
  @Input() component: ModalContentData;
  @Output() passEntry: EventEmitter<any> = new EventEmitter();
  @ViewChild(ModalDirective) modalContent: ModalDirective;

  emitData: any; // temp data for two way binding
  selectedIndex: number = 0; // number to switch tab
  componentFactory: any;
  viewContainerRef: any;
  componentRef: any;

  constructor(
    public activeModal: NgbActiveModal,
    private componentFactoryResolver: ComponentFactoryResolver
  ) {}

  ngOnInit() {
    this.loadComponent();
  }

  loadComponent() {
    this.componentFactory = this.componentFactoryResolver.resolveComponentFactory(
      this.component.modalComponent
    );

    this.viewContainerRef = this.modalContent.viewContainerRef;
    this.viewContainerRef.clear();

    this.componentRef = this.viewContainerRef.createComponent(
      this.componentFactory
    );

    this.emitData = Object.assign({}, this.component.data);
    (<ModalInterface>this.componentRef.instance).data = this.emitData;
    (<ModalInterface>this.componentRef.instance).mode = this.mode;
    (<ModalInterface>(
      this.componentRef.instance
    )).selectedIndex = this.selectedIndex;
  }

  nextPage() {
    (<ModalInterface>this.componentRef.instance).selectedIndex++;
  }

  passBack() {
    this.component.data = this.emitData;
    this.passEntry.emit(this.component.data);
  }
}
