/*
* ============LICENSE_START=======================================================
* ONAP : DATALAKE
* ================================================================================
* Copyright 2019-2020 China Mobile
* Copyright (C) 2021 Wipro Limited
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

CREATE TABLE topic_name (
  id varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE db_type (
  id varchar(255) NOT NULL,
  default_port int DEFAULT NULL,
  name varchar(255) NOT NULL,
  tool boolean NOT NULL,
  PRIMARY KEY (id)
);

CREATE SEQUENCE db_seq;

CREATE TABLE db (
  id int NOT NULL DEFAULT NEXTVAL ('db_seq'),
  database_name varchar(255) DEFAULT NULL,
  enabled boolean NOT NULL,
  encrypt boolean DEFAULT NULL,
  host varchar(255) DEFAULT NULL,
  login varchar(255) DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  pass varchar(255) DEFAULT NULL,
  port int DEFAULT NULL,
  property1 varchar(255) DEFAULT NULL,
  property2 varchar(255) DEFAULT NULL,
  property3 varchar(255) DEFAULT NULL,
  db_type_id varchar(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK3njadtw43ieph7ftt4kxdhcko FOREIGN KEY (db_type_id) REFERENCES db_type (id)
);

CREATE INDEX FK3njadtw43ieph7ftt4kxdhcko ON db (db_type_id);

CREATE TABLE design_type (
  id varchar(255) NOT NULL,
  name varchar(255) DEFAULT NULL,
  note varchar(255) DEFAULT NULL,
  db_type_id varchar(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FKm8rkv2qkq01gsmeq1c3y4w02x FOREIGN KEY (db_type_id) REFERENCES db_type (id)
);

CREATE INDEX FKm8rkv2qkq01gsmeq1c3y4w02x ON design_type (db_type_id);

CREATE SEQUENCE design_seq;

CREATE TABLE design (
  id int NOT NULL DEFAULT NEXTVAL ('design_seq'),
  body text DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  note varchar(255) DEFAULT NULL,
  submitted boolean DEFAULT NULL,
  design_type_id varchar(255) NOT NULL,
  topic_name_id varchar(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FKabb8e74230glxpaiai4aqsr34 FOREIGN KEY (topic_name_id) REFERENCES topic_name (id),
  CONSTRAINT FKo43yi6aputq6kwqqu8eqbspm5 FOREIGN KEY (design_type_id) REFERENCES design_type (id)
);

CREATE INDEX FKo43yi6aputq6kwqqu8eqbspm5 ON design (design_type_id);
CREATE INDEX FKabb8e74230glxpaiai4aqsr34 ON design (topic_name_id);

CREATE SEQUENCE kafka_seq;

CREATE TABLE kafka (
  id int NOT NULL DEFAULT NEXTVAL ('kafka_seq'),
  broker_list varchar(255) NOT NULL,
  consumer_count int DEFAULT 3,
  enabled boolean NOT NULL,
  excluded_topic varchar(1023) DEFAULT '__consumer_offsets,__transaction_state',
  "group" varchar(255) DEFAULT 'datalake',
  included_topic varchar(255) DEFAULT NULL,
  login varchar(255) DEFAULT NULL,
  name varchar(255) NOT NULL,
  pass varchar(255) DEFAULT NULL,
  secure boolean DEFAULT FALSE,
  security_protocol varchar(255) DEFAULT NULL,
  timeout_sec int DEFAULT 10,
  zk varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE SEQUENCE topic_seq;

CREATE TABLE topic (
  id int NOT NULL DEFAULT NEXTVAL ('topic_seq'),
  aggregate_array_path varchar(255) DEFAULT NULL,
  correlate_cleared_message boolean NOT NULL DEFAULT FALSE,
  data_format varchar(255) DEFAULT NULL,
  enabled boolean NOT NULL,
  flatten_array_path varchar(255) DEFAULT NULL,
  login varchar(255) DEFAULT NULL,
  message_id_path varchar(255) DEFAULT NULL,
  pass varchar(255) DEFAULT NULL,
  save_raw boolean NOT NULL DEFAULT FALSE,
  ttl_day int DEFAULT NULL,
  topic_name_id varchar(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FKj3pldlfaokdhqjfva8n3pkjca FOREIGN KEY (topic_name_id) REFERENCES topic_name (id)
);

CREATE INDEX FKj3pldlfaokdhqjfva8n3pkjca ON topic (topic_name_id);

CREATE TABLE map_db_design (
  design_id int NOT NULL,
  db_id int NOT NULL,
  PRIMARY KEY (design_id,db_id),
  CONSTRAINT FKfli240v96cfjbnmjqc0fvvd57 FOREIGN KEY (design_id) REFERENCES design (id),
  CONSTRAINT FKhpn49r94k05mancjtn301m2p0 FOREIGN KEY (db_id) REFERENCES db (id)
);

CREATE INDEX FKhpn49r94k05mancjtn301m2p0 ON map_db_design (db_id);

CREATE TABLE map_db_topic (
  topic_id int NOT NULL,
  db_id int NOT NULL,
  PRIMARY KEY (db_id,topic_id),
  CONSTRAINT FKirro29ojp7jmtqx9m1qxwixcc FOREIGN KEY (db_id) REFERENCES db (id),
  CONSTRAINT FKq1jon185jnrr7dv1dd8214uw0 FOREIGN KEY (topic_id) REFERENCES topic (id)
);

CREATE INDEX FKq1jon185jnrr7dv1dd8214uw0 ON map_db_topic (topic_id);

CREATE TABLE map_kafka_topic (
  kafka_id int NOT NULL,
  topic_id int NOT NULL,
  PRIMARY KEY (topic_id,kafka_id),
  CONSTRAINT FK5q7jdxy54au5rcrhwa4a5igqi FOREIGN KEY (topic_id) REFERENCES topic (id),
  CONSTRAINT FKtdrme4h7rxfh04u2i2wqu23g5 FOREIGN KEY (kafka_id) REFERENCES kafka (id)
);

CREATE INDEX FKtdrme4h7rxfh04u2i2wqu23g5 ON map_kafka_topic (kafka_id);

