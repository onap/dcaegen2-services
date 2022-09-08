/*
    Copyright (C) 2019 CMCC, Inc. and others. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/


/**
 * This component is designed for all kinds of Button.
 *
 * @author Xu Ran
 *
 * @contributor Chunmeng Guo
 *
 */

import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';

@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.css']
})
export class ButtonComponent implements OnInit {
  /**
   * This component supports thress properties.
      * text: 
        * All strings are ok for block and inline sytle. MAKE SURE that icon and inlineicon style button needs specific text. 
        * This property is used for defined the function of the button. E.g. if you what a "cancel" button, please input a 'Cancel' string to this property and an 'add' string is for 'add' button
      * style: four properties is available: block, inline, inlineicon, icon. This property is used for the style of the button. We support three button styles.
      * color: two properties is available: dark, light. This property is used for the color of the button. dark button is filled, light button is unfilled. 
   */

  @Input() text: string;
  @Input() style: string;
  @Input() color: string;
  @Output() btnAction = new EventEmitter<object>()
  buttonstyle: number;
  buttoncolor: number;
  constructor() { }

  ngOnInit() {
    switch (this.style) {
      case 'block':
        this.buttonstyle = 1;
        break;
      case 'inline':
        this.buttonstyle = 2;
        break;
      case 'icon':
        this.buttonstyle = 3;
        break;
      case 'inlineicon':
        this.buttonstyle = 4;
        break;
    }

    switch (this.color) {
      case 'light':
        this.buttoncolor = 1;
        break;
      case 'dark':
        this.buttoncolor = 2;
        break;
    }
  }

  buttonClick(string) {
    this.btnAction.emit(string);
  }
}
