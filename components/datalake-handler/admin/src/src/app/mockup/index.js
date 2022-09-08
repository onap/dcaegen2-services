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

// Database
const low = require("lowdb");
const FileSync = require("lowdb/adapters/FileSync");
const adapter = new FileSync("src/app/mockup/db.json");
const db = low(adapter);

// Json Server
const jsonServer = require("json-server");
const router = jsonServer.router(db);
const server = jsonServer.create();
const middlewares = jsonServer.defaults();

// DataLake parameter
const endpoint = "/datalake/v1";
const port = 1680;
var feederStatus = true;
var feederVersion = "v1.0.0";

server.use(middlewares);
server.use(jsonServer.bodyParser);

// Methods
const postData = (func, req, res) => {
  db.get(func)
    .push(req.body)
    .write();

  let d = db.get(func).find({
    name: req.body.name
  });

  let response = {
    statusCode: 200,
    returnBody: d
  };

  res.status(200).jsonp(response);
};

const putData = (func, req, res) => {
  db.get(func)
    .find({
      name: req.body.name
    })
    .assign(req.body)
    .write();

  let d = db.get(func).find({
    name: req.body.name
  });

  let response = {
    statusCode: 200,
    returnBody: d
  };

  res.status(200).jsonp(response);
};

const deleteData = (func, req, res) => {
  db.get(func)
    .remove({
      name: req.params.name
    })
    .write();

  res.status(204).jsonp({});
};

// REST API: /dbs
server.post(endpoint + "/dbs", async (req, res) => {
  postData("dbs", req, res);
});

server.put(endpoint + "/dbs/:name", async (req, res) => {
  putData("dbs", req, res);
});

server.delete(endpoint + "/dbs/:name", async (req, res) => {
  deleteData("dbs", req, res);
});

server.post(endpoint + "/dbs/verify", async (req, res) => {
  res.status(200).jsonp({
    verify: true
  });
});
// End REST API: /dbs

// REST API: /topics
server.post(endpoint + "/topics", async (req, res) => {
  postData("topics", req, res);
});

server.put(endpoint + "/topics/:id", async (req, res) => {
  putData("topics", req, res);
});

server.delete(endpoint + "/topics/:id", async (req, res) => {
  deleteData("topics", req, res);
});
// End REST API: /topics

// REST API: /feeder
server.get(endpoint + "/feeder/status", (req, res) => {
  let response = {
    running: feederStatus,
    version: feederVersion
  };

  res.status(200).jsonp(response);
});

server.post(endpoint + "/feeder/start", (req, res) => {
  feederStatus = true;
  let response = {
    running: feederStatus
  };

  res.status(200).jsonp(response);
});

server.post(endpoint + "/feeder/stop", (req, res) => {
  feederStatus = false;
  let response = {
    running: feederStatus
  };

  res.status(200).jsonp(response);
});
// End REST API: /feeder

// Custom render data
router.render = (req, res) => {
  if (req.method === "GET") {
    // Return a array for dbs, topics
    switch (req.originalUrl) {
      case endpoint + "/dbs":
      case endpoint + "/topics":
      case endpoint + "/topics/dmaap":
        let obj = res.locals.data;
        let data = [];
        for (let i = 0; i < obj.length; i++) {
          data.push(obj[i].id);
        }
        res.jsonp(data);
        break;
      default:
        res.jsonp(res.locals.data[0]);
    }
  }
};

// Custom routes
// Add this before server.use(router)
server.use(
  jsonServer.rewriter({
    "/datalake/v1/dbs/:id": "/dbs?id=:id",
    "/datalake/v1/topics/_DL_DEFAULT_": "/topics?name=_DL_DEFAULT_",
    "/datalake/v1/topics/:id": "/topics?id=:id",
    "/datalake/v1/db_type": "/db_type"
  })
);

server.use(endpoint, router);
server.use(router);

server.listen(port, () => {
  console.log("JSON Server is running, http://localhost:" + port);
});
