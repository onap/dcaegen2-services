/*
* ============LICENSE_START=======================================================
* ONAP : DATALAKE
* ================================================================================
* Copyright 2019 China Mobile
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

INSERT INTO kafka(
   id
  ,name
  ,consumer_count
  ,enabled
  ,"group"
  ,broker_list
  ,included_topic
  ,login
  ,pass
  ,secure
  ,security_protocol
  ,timeout_sec
  ,zk
) VALUES (
  1
  ,'main Kafka cluster' -- name - IN varchar (255)
  ,3   -- consumer_count - IN int(11)
  ,true   -- enabled - IN bit(1)
  ,'dlgroup'  -- group - IN varchar(255)
  ,'message-router-kafka:9092'  -- host_port - IN varchar(255)
  ,''  -- included_topic - IN varchar(255)
  ,'admin'  -- login - IN varchar(255)
  ,'admin-secret'  -- pass - IN varchar(255)
  ,false   -- secure - IN bit(1)
  ,'SASL_PLAINTEXT'  -- security_protocol - IN varchar(255)
  ,10   -- timeout_sec - IN int(11)
  ,'message-router-zookeeper:2181'  -- zk - IN varchar(255)
);

insert into db_type (id, name, tool) values ('CB', 'Couchbase', false);
insert into db_type (id, name, tool) values ('ES', 'Elasticsearch', false);
insert into db_type (id, name, tool,default_port) values ('MONGO', 'MongoDB', false, 27017);
insert into db_type (id, name, tool) values ('DRUID', 'Druid', false);
insert into db_type (id, name, tool) values ('HDFS', 'HDFS', false);
insert into db_type (id, name, tool) values ('KIBANA', 'Kibana', true);
insert into db_type (id, name, tool) values ('SUPERSET', 'Apache Superset', true);

insert into db (id, db_type_id, enabled, encrypt, name,host,login,pass,database_name) values (1, 'CB', true, true, 'Couchbase 1','dl-couchbase','dl','dl1234','datalake');
insert into db (id, db_type_id, enabled, encrypt, name,host) values (2, 'ES', true, true, 'Elasticsearch','dl-es');
insert into db (id, db_type_id, enabled, encrypt, name,host,port,database_name) values (3, 'MONGO', true, true, 'MongoDB 1','dl-mongodb',27017,'datalake');
insert into db (id, db_type_id, enabled, encrypt, name,host) values (4, 'DRUID', true, true, 'Druid','dl-druid');
insert into db (id, db_type_id, enabled, encrypt, name,host,login) values (5, 'HDFS', true, true, 'Hadoop Cluster','dl-hdfs','dl');
insert into db (id, db_type_id, enabled, encrypt, name,host) values (6, 'KIBANA', true, false, 'Kibana demo','dl-es');
insert into db (id, db_type_id, enabled, encrypt, name,host) values (7, 'SUPERSET', true, false, 'Superset demo','dl-druid');

insert into topic_name (id) values ('_DL_DEFAULT_');
insert into topic_name (id) values ('unauthenticated.SEC_FAULT_OUTPUT');
insert into topic_name (id) values ('unauthenticated.VES_MEASUREMENT_OUTPUT');
insert into topic_name (id) values ('EPC');
insert into topic_name (id) values ('HW');

-- SQLINES DEMO *** fault enabled should be off
insert into topic(id, topic_name_id,enabled,save_raw,ttl_day,data_format) values (1, '_DL_DEFAULT_',true,false,3650,'JSON');

insert into topic(id, topic_name_id,correlate_cleared_message,enabled, message_id_path,data_format)
values (2, 'unauthenticated.SEC_FAULT_OUTPUT',true,true,'/event/commonEventHeader/eventName,/event/commonEventHeader/reportingEntityName,/event/faultFields/specificProblem,/event/commonEventHeader/eventId','JSON');

insert into topic(id, topic_name_id,enabled, aggregate_array_path,flatten_array_path,data_format)
values (3, 'unauthenticated.VES_MEASUREMENT_OUTPUT',true,
'/event/measurementsForVfScalingFields/memoryUsageArray,/event/measurementsForVfScalingFields/diskUsageArray,/event/measurementsForVfScalingFields/cpuUsageArray,/event/measurementsForVfScalingFields/vNicPerformanceArray',
'/event/measurementsForVfScalingFields/astriMeasurement/astriDPMeasurementArray/astriInterface',
'JSON');

insert into topic(id, topic_name_id,enabled,  flatten_array_path,data_format)
values (4, 'EPC',true, '/event/measurementsForVfScalingFields/astriMeasurement/astriDPMeasurementArray/astriInterface', 'JSON');

insert into topic(id, topic_name_id,enabled, aggregate_array_path,data_format)
values (5, 'HW',true,
'/event/measurementsForVfScalingFields/memoryUsageArray,/event/measurementsForVfScalingFields/diskUsageArray,/event/measurementsForVfScalingFields/cpuUsageArray,/event/measurementsForVfScalingFields/vNicPerformanceArray',
'JSON');


insert into map_db_topic(db_id,topic_id) select db.id, topic.id from db_type, db, topic where db.db_type_id=db_type.id and db_type.tool=false;
insert into map_kafka_topic(kafka_id,topic_id) select kafka.id, topic.id from kafka, topic;


insert into design_type (id, name, db_type_id) values ('KIBANA_DB', 'Kibana Dashboard', 'KIBANA');
insert into design_type (id, name, db_type_id) values ('KIBANA_SEARCH', 'Kibana Search', 'KIBANA');
insert into design_type (id, name, db_type_id) values ('KIBANA_VISUAL', 'Kibana Visualization', 'KIBANA');
insert into design_type (id, name, db_type_id) values ('ES_MAPPING', 'Elasticsearch Field Mapping Template', 'ES');
insert into design_type (id, name, db_type_id) values ('DRUID_KAFKA_SPEC', 'Druid Kafka Indexing Service Supervisor Spec', 'DRUID');


insert into design (id, name,topic_name_id, submitted,body, design_type_id) values (1, 'Kibana Dashboard on EPC test1', 'EPC',  false, 'body here', 'KIBANA_DB');

insert into map_db_design (design_id,db_id ) values (1, 6);
