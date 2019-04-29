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
