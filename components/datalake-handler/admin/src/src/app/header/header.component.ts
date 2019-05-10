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

import { Component } from "@angular/core";
import { HeaderService } from "../core/services/header.service";
import { TranslateService } from "@ngx-translate/core";

@Component({
  selector: "app-header",
  templateUrl: "./header.component.html",
  styleUrls: ["./header.component.css"]
})
export class HeaderComponent {
  title = "PageTitle";

  selectedLang: String;
  langs: Array<any> = [
    { value: "en-us", name: "EN" },
    { value: "zh-hans", name: "中文(简)" },
    { value: "zh-hant", name: "中文(繁)" }
  ];

  constructor(
    private headerService: HeaderService,
    private translateService: TranslateService
  ) {
    this.translateService.setDefaultLang("en-us");
  }

  ngOnInit() {
    this.headerService.title.subscribe(title => {
      this.title = title;
    });
    this.selectedLang = "en-us";
  }

  changeLanguage(lang: string) {
    console.log("Selected:" + lang);
    this.translateService.use(lang);
  }
}
