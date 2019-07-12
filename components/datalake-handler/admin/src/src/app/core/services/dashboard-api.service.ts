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
import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Observable, of } from "rxjs";
import { map, catchError, tap, retry } from "rxjs/operators";
import { throwError } from "rxjs";

import { Template, newTemplate } from "src/app/core/models/template.model";
import { Dashboard } from "src/app/core/models/dashboard.model";

const prefix = "/datalake/v1/";

@Injectable({
  providedIn: 'root'
})
export class DashboardApiService {

  constructor(private http: HttpClient) {
  }

  private extractData(res: Response) {
    if (res.status < 200 || res.status >= 300) {
      throw new Error("Bad response status: " + res.status);
    }
    let body = res;
    return body || {};
  }

  private handleError(error) {
    let errorMessage = "";
    if (error.error instanceof ErrorEvent) {
      // Get client-side error
      errorMessage = error.error.message;
    } else {
      // Get server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      console.log(errorMessage);
    }
    return throwError(errorMessage);
  }

  private extractData2(res: Response) {
    // console.log(res, "detele/deploy template");
    let body = res;
    return body || {};
  }


  /*
  Dashboard
*/
  getDashboardList(): Observable<any> {
    let url = prefix + "portals"; //onilne
    return this.http.get(url).pipe(
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }

  createUpadteDashboard(d: Dashboard): Observable<any> {
    // let url = prefix +"/dashboard-list/successCreteOrEditDemo.json"; //local
    let url = prefix + "portals";//onilne
    return this.http
      .put(url, d)
      .pipe(
        retry(1),
        tap(_ => this.extractData),
        catchError(this.handleError)
      );
  }

  deleteDashboard(d: Dashboard): Observable<any> {
    let url = prefix + "portals"; //onilne
    return this.http
      .put(url, d)
      .pipe(
        retry(1),
        tap(_ => console.log(`deleted db name=${d.name}`)),
        catchError(this.handleError)
      );
  }

  // getDashboardName(): Observable<any> {
  //   // let url = prefix +"/dashboard-list/getDashboardName.json"; //local
  //   let url = prefix + "portals/getNames?enabled=false"; //onilne
  //   return this.http.get(url).pipe(
  //     retry(1),
  //     map(this.extractData),
  //     catchError(this.handleError)
  //   );
  // }

  /*
    Template
  */
  getTemplateAll(): Observable<any> {
    return this.http.get(prefix + "designs/").pipe( //onlin
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }

  createNewTemplate(t: newTemplate): Observable<any> {
    return this.http
      .post(prefix + "designs", t)
      .pipe(
        retry(1),
        tap(_ => this.extractData),
        catchError(this.handleError)
      );
  }

  updateNewTemplate(t: Template): Observable<any> {
    let id = t.id;
    return this.http
      .put(prefix + "designs/" + id, t)
      .pipe(
        retry(1),
        tap(_ => this.extractData),
        catchError(this.handleError)
      );
  }

  getTopicName(): Observable<any> {
    return this.http.get(prefix + "topics").pipe( //onlin
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }

  getTemplateTypeName(): Observable<any> {
    return this.http.get(prefix + "designTypes").pipe( //onlin
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }

  DeleteTemplate(id): Observable<any> {
    return this.http.delete(prefix + "designs/" + id).pipe( //online
      retry(1),
      map(this.extractData2),
      catchError(this.handleError)
    );
  }
  deployTemplateKibana(id, body): Observable<any> {
    body.submitted = true;
    return this.http.post(prefix + "designs/deploy/" + id, body).pipe(   //online
      retry(1),
      map(this.extractData2),
      catchError(this.handleError)
    );
  }
}
