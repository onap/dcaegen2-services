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

import { Topic,newTopic } from "src/app/core/models/topic.model";
import { Db } from "src/app/core/models/db.model";

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

  addNewTopic(t: newTopic): Observable<any> {
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
  
  upadteDb(d: Db): Observable<any> {
    return this.http
      .put(prefix + "dbs", d)
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
}
