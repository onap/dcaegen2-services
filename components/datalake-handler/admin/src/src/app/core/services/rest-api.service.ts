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
 * This service is to callback REST API from feeder
 *
 * @author Ekko Chang
 *
 */

import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Observable, of } from "rxjs";
import { map, catchError, tap, retry } from "rxjs/operators";
import { throwError } from "rxjs";

import { Topic } from "src/app/core/models/topic.model";
import { Db } from "src/app/core/models/db.model";
import { Template } from "src/app/core/models/template.model";
import { Dashboard } from "src/app/core/models/dashboard.model";
import {Kafka} from "../models/kafka.model";

const prefix = "/datalake/v1/";
const httpOptions = {
  headers: new HttpHeaders({
    "Content-Type": "application/json"
  })
};

@Injectable({
  providedIn: "root"
})
export class RestApiService {
  constructor(private http: HttpClient) { }

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
    let body = res;
    return body || {};
  }

  /*
    Topic default config
  */
  getTopicDefaultConfig(): Observable<any> {
    return this.http.get(prefix + "topics/_DL_DEFAULT_").pipe(
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }

  updateTopicDefaultConfig(t: Topic): Observable<any> {
    return this.http
      .put(prefix + "topics/_DL_DEFAULT_", JSON.stringify(t), httpOptions)
      .pipe(
        retry(1),
        tap(_ => this.extractData),
        catchError(this.handleError)
      );
  }

  /*
    Topics
  */
  getTopicsFromDmaap(): Observable<any> {
    return this.http.get(prefix + "topics/dmaap").pipe(
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }

  getTopicsFromFeeder(): Observable<any> {
    return this.http.get(prefix + "topics").pipe(
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }

  getTopicDetail(name: string): Observable<any> {
    return this.http.get(prefix + "topics/" + name).pipe(
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }

  addNewTopic(t: Topic): Observable<any> {
    return this.http
      .post<any>(prefix + "topics", t)
      .pipe(
        retry(1),
        tap(_ => console.log(`add topic name=${t.name}`)),
        catchError(this.handleError)
      );
  }

  addTopic(t: Topic): Observable<any> {
    return this.http
      .post<any>(prefix + "topics", t)
      .pipe(
        retry(1),
        tap(_ => console.log(`add topic name=${t.name}`)),
        catchError(this.handleError)
      );
  }

  upadteTopic(t: Topic): Observable<any> {
    return this.http
      .put(prefix + "topics/" + t.name, t)
      .pipe(
        retry(1),
        tap(_ => this.extractData),
        catchError(this.handleError)
      );
  }

  deleteTopic(name: string): Observable<any> {
    return this.http.delete(prefix + "topics/" + name).pipe(
      retry(1),
      tap(_ => console.log(`deleted topic name=${name}`)),
      catchError(this.handleError)
    );
  }

  /*
    Database
  */
  getDbEncryptList(flag): Observable<any> {
    return this.http.get(prefix + "dbs/list/?encrypt="+flag).pipe(
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }

  getDbList(): Observable<any> {
    return this.http.get(prefix + "dbs").pipe(
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }

  getDbDetail(name: string): Observable<any> {
    return this.http.get(prefix + "dbs/" + name).pipe(
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }

  deleteDb(id): Observable<any> {
    return this.http.delete(prefix + "dbs/" + id).pipe( //online
      retry(1),
      map(this.extractData2),
      catchError(this.handleError)
    );
  }

  updateDb(d: Db): Observable<any> {
    return this.http
      .put(prefix + "dbs", d)
      .pipe(
        retry(1),
        tap(_ => this.extractData),
        catchError(this.handleError)
      );
  }

  createDb(d: Db): Observable<any> {
    return this.http
      .post(prefix + "dbs", d)
      .pipe(
        retry(1),
        tap(_ => this.extractData),
        catchError(this.handleError)
      );
  }

  /*
    Feeder
  */
  startFeeder() {
    return this.http.post<any>(prefix + "feeder/start", "", httpOptions).pipe(
      retry(1),
      tap(_ => console.log(`start feeder`)),
      catchError(this.handleError)
    );
  }

  stopFeeder() {
    return this.http.post<any>(prefix + "feeder/stop", "", httpOptions).pipe(
      retry(1),
      tap(_ => console.log(`stop feeder`)),
      catchError(this.handleError)
    );
  }

  getFeederstatus() {
    return this.http.get(prefix + "feeder/status").pipe(
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
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

  getTempDbList(id): Observable<any> {
    return this.http.get(prefix + "dbs/idAndName/" + id).pipe(
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }

  createNewTemplate(t: Template): Observable<any> {
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

  // getTopicName(): Observable<any> {
  //   return this.http.get(prefix + "topics").pipe( //onlin
  //     retry(1),
  //     map(this.extractData),
  //     catchError(this.handleError)
  //   );
  // }

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

  /*
  Kafka
*/
  getAllKafkaList() {
    return this.http.get(prefix + "kafkas").pipe( //online
      retry(1),
      map(this.extractData),
      catchError(this.handleError)
    );
  }
  deleteKafka(id): Observable<any> {
    return this.http.delete(prefix + "kafkas/" + id).pipe( //online
      retry(1),
      map(this.extractData2),
      catchError(this.handleError)
    );
  }

  createNewKafka(k: Kafka): Observable<any> {
    return this.http
      .post(prefix + "kafkas", k)
      .pipe(
        retry(1),
        tap(_ => this.extractData),
        catchError(this.handleError)
      );
  }

  updateKafka(k: Kafka): Observable<any> {
    let id = k.id;
    return this.http
      .put(prefix + "kafkas/" + id, k)
      .pipe(
        retry(1),
        tap(_ => this.extractData),
        catchError(this.handleError)
      );
  }
}


