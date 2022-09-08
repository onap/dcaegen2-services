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
import { Db, DbType } from "src/app/core/models/db.model";
import { Template } from "src/app/core/models/template.model";
import { Dashboard } from "src/app/core/models/dashboard.model";
import { Kafka } from "../models/kafka.model";

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
  constructor(private http: HttpClient) {}

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
    Topics
  */
  public getTopicDefault(): Observable<Topic> {
    return this.http
      .get<Topic>(prefix + "topics/default")
      .pipe(retry(1), catchError(this.handleError));
  }

  public getTopicList(): Observable<string[]> {
    return this.http
      .get<string[]>(prefix + "topics")
      .pipe(retry(1), catchError(this.handleError));
  }

  public getTopicListFromKafka(id: string | number): Observable<string[]> {
    return this.http
      .get<string[]>(prefix + "topics/dmaap/" + id)
      .pipe(retry(1), catchError(this.handleError));
  }

  // Get topic names for adding
  public getTopicNames(): Observable<string[]> {
    return this.http
      .get<string[]>(prefix + "topicNames")
      .pipe(retry(1), catchError(this.handleError));
  }

  public getTopic(id: string | number): Observable<Topic> {
    return this.http
      .get<Topic>(prefix + "topics/" + id)
      .pipe(retry(1), catchError(this.handleError));
  }

  public updateTopic(t: Topic): Observable<Topic> {
    return this.http.put<Topic>(prefix + "topics/" + t.id, t).pipe(
      retry(1),
      tap(_ => this.extractData),
      catchError(this.handleError)
    );
  }

  public addTopic(t: Topic): Observable<Topic> {
    return this.http.post<Topic>(prefix + "topics", t).pipe(
      retry(1),
      tap(_ => console.log(`add topic name=${t.name}`)),
      catchError(this.handleError)
    );
  }

  public deleteTopic(id: number | string): Observable<Topic> {
    return this.http.delete<Topic>(prefix + "topics/" + id).pipe(
      retry(1),
      tap(_ => console.log(`deleted topic name=${name}`)),
      catchError(this.handleError)
    );
  }

  // TODO
  getTopicsFromFeeder(): Observable<any> {
    return this.http
      .get(prefix + "topics")
      .pipe(retry(1), map(this.extractData), catchError(this.handleError));
  }

  /*
    Database and tools
  */
  public getAllDbs(): Observable<Db[]> {
    return this.http
      .get<Db[]>(prefix + "dbs/list?isDb=true")
      .pipe(retry(1), catchError(this.handleError));
  }

  public getAllTools(): Observable<Db[]> {
    return this.http
      .get<Db[]>(prefix + "dbs/list?isDb=false")
      .pipe(retry(1), catchError(this.handleError));
  }

  public getDbTypes(): Observable<DbType[]> {
    return this.http
      .get<DbType[]>(prefix + "dbs/dbtypes")
      .pipe(retry(1), catchError(this.handleError));
  }

  public deleteDb(id: string | number): Observable<Db> {
    return this.http.delete<Db>(prefix + "dbs/" + id).pipe(
      //online
      retry(1),
      tap(_ => console.log(`deleted db id=${id}`)),
      catchError(this.handleError)
    );
  }

  public updateDb(db: Db): Observable<Db> {
    return this.http.put<Db>(prefix + "dbs/" + db.id, db).pipe(
      retry(1),
      tap(_ => this.extractData),
      catchError(this.handleError)
    );
  }

  public addDb(db: Db): Observable<Db> {
    return this.http.post<Db>(prefix + "dbs", db).pipe(
      retry(1),
      tap(_ => console.log(`add db name=${db.name}`)),
      catchError(this.handleError)
    );
  }

  getDbEncryptList(flag): Observable<any> {
    return this.http
      .get(prefix + "dbs/list?isDb=" + flag)
      .pipe(retry(1), map(this.extractData), catchError(this.handleError));
  }

  getDbList(): Observable<any> {
    return this.http
      .get(prefix + "dbs")
      .pipe(retry(1), map(this.extractData), catchError(this.handleError));
  }

  getDbDetail(id): Observable<any> {
    return this.http
      .get(prefix + "dbs/" + id)
      .pipe(retry(1), map(this.extractData), catchError(this.handleError));
  }

  createDb(d: Db): Observable<any> {
    return this.http.post(prefix + "dbs", d).pipe(
      retry(1),
      tap(_ => this.extractData),
      catchError(this.handleError)
    );
  }

  // Deprecated
  getDbTypeList(): Observable<any> {
    return this.http
      .get(prefix + "db_type")
      .pipe(retry(1), map(this.extractData), catchError(this.handleError));
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
    return this.http
      .get(prefix + "feeder/status")
      .pipe(retry(1), map(this.extractData), catchError(this.handleError));
  }

  /*
  Dashboard
  */
  getDashboardList(): Observable<any> {
    let url = prefix + "portals"; //onilne
    return this.http
      .get(url)
      .pipe(retry(1), map(this.extractData), catchError(this.handleError));
  }

  createUpadteDashboard(d: Dashboard): Observable<any> {
    // let url = prefix +"/dashboard-list/successCreteOrEditDemo.json"; //local
    let url = prefix + "portals"; //onilne
    return this.http.put(url, d).pipe(
      retry(1),
      tap(_ => this.extractData),
      catchError(this.handleError)
    );
  }

  deleteDashboard(d: Dashboard): Observable<any> {
    let url = prefix + "portals"; //onilne
    return this.http.put(url, d).pipe(
      retry(1),
      tap(_ => console.log(`deleted db name=${d.name}`)),
      catchError(this.handleError)
    );
  }

  /*
    Template
  */
  public getAllTemplate(): Observable<Template[]> {
    return this.http
      .get<Template[]>(prefix + "designs/")
      .pipe(retry(1), catchError(this.handleError));
  }

  public addTemplate(t: Template): Observable<Template> {
    return this.http.post<Template>(prefix + "designs", t).pipe(
      retry(1),
      tap(_ => console.log(`add template name=${t.name}`)),
      catchError(this.handleError)
    );
  }

  public updateTemplate(t: Template): Observable<Template> {
    return this.http.put<Template>(prefix + "designs/" + t.id, t).pipe(
      retry(1),
      tap(_ => this.extractData),
      catchError(this.handleError)
    );
  }

  public deleteTemplate(id: number): Observable<Template> {
    return this.http.delete<Template>(prefix + "designs/" + id).pipe(
      retry(1),
      tap(_ => console.log(`deleted template id=${id}`)),
      catchError(this.handleError)
    );
  }

  getTempDbList(id): Observable<any> {
    return this.http
      .get(prefix + "dbs/idAndName/" + id)
      .pipe(retry(1), map(this.extractData), catchError(this.handleError));
  }

  public getTemplateDesignType(): Observable<any> {
    return this.http
      .get(prefix + "designTypes")
      .pipe(retry(1), map(this.extractData), catchError(this.handleError));
  }

  public deployTemplateKibana(id: number, data: Template): Observable<any> {
    data.submitted = true;
    return this.http.post(prefix + "designs/deploy/" + id, data).pipe(
      retry(1),
      tap(_ => console.log(`deploy template id=${data.id}`)),
      catchError(this.handleError)
    );
  }

  /*
    Kafka
  */
  public getAllKafka(): Observable<Kafka[]> {
    return this.http
      .get<Kafka[]>(prefix + "kafkas")
      .pipe(retry(1), catchError(this.handleError));
  }

  public getKafka(id: string | number): Observable<Kafka> {
    return this.http
      .get<Kafka>(prefix + "kafkas/" + id)
      .pipe(retry(1), catchError(this.handleError));
  }

  public updateKafka(k: Kafka): Observable<Kafka> {
    return this.http.put<Kafka>(prefix + "kafkas/" + k.id, k).pipe(
      retry(1),
      tap(_ => this.extractData),
      catchError(this.handleError)
    );
  }

  public addKafka(k: Kafka): Observable<Kafka> {
    return this.http.post<Kafka>(prefix + "kafkas", k).pipe(
      retry(1),
      tap(_ => this.extractData),
      catchError(this.handleError)
    );
  }

  public deleteKafka(id: string | number): Observable<Kafka> {
    return this.http.delete<Kafka>(prefix + "kafkas/" + id).pipe(
      retry(1),
      tap(_ => console.log(`deleted kafka id=${id}`)),
      catchError(this.handleError)
    );
  }
}
