
/*
* ============LICENSE_START=======================================================
* ONAP : DATALAKE
* ================================================================================
* Copyright 2020 China Mobile
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
\c datalake
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
  ,B'1'   -- enabled - IN bit(1)
  ,'dlgroup'  -- group - IN varchar(255)
  ,'message-router-kafka:9092'  -- host_port - IN varchar(255)
  ,''  -- included_topic - IN varchar(255)
  ,'admin'  -- login - IN varchar(255)
  ,'admin-secret'  -- pass - IN varchar(255)
  ,B'0'   -- secure - IN bit(1)
  ,'SASL_PLAINTEXT'  -- security_protocol - IN varchar(255)
  ,10   -- timeout_sec - IN int(11)
  ,'message-router-zookeeper:2181'  -- zk - IN varchar(255)
);
insert into db_type (id, name, tool) values ('CB', 'Couchbase', B'0');
insert into db_type (id, name, tool) values ('ES', 'Elasticsearch', B'0');
insert into db_type (id, name, tool,default_port) values ('MONGO', 'MongoDB', B'0', 27017);
insert into db_type (id, name, tool) values ('DRUID', 'Druid', B'0');
insert into db_type (id, name, tool) values ('HDFS', 'HDFS', B'0');
insert into db_type (id, name, tool) values ('KIBANA', 'Kibana', B'1');
insert into db_type (id, name, tool) values ('SUPERSET', 'Apache Superset', B'1');
insert into db (id, db_type_id, enabled, encrypt, name,host,login,pass,database_name) values (1, 'CB', B'1', B'1', 'Couchbase 1','dl-couchbase','dl','dl1234','datalake');
insert into db (id, db_type_id, enabled, encrypt, name,host) values (2, 'ES', B'1', B'1', 'Elasticsearch','dl-es');
insert into db (id, db_type_id, enabled, encrypt, name,host,port,database_name,presto_catalog) values (3, 'MONGO', B'1', B'1', 'MongoDB 1','dl-mongodb',27017,'datalake','mongodb');
insert into db (id, db_type_id, enabled, encrypt, name,host) values (4, 'DRUID', B'1',B'1', 'Druid','dl-druid');
insert into db (id, db_type_id, enabled, encrypt, name,host,login) values (5, 'HDFS', B'1', B'1', 'Hadoop Cluster','dl-hdfs','dl');
insert into db (id, db_type_id, enabled, encrypt, name,host) values (6, 'KIBANA', B'1', B'0', 'Kibana demo','dl-es');
insert into db (id, db_type_id, enabled, encrypt, name,host) values (7, 'SUPERSET', B'1', B'0', 'Superset demo','dl-druid');
insert into topic_name (id) values ('_DL_DEFAULT_');
insert into topic_name (id) values ('unauthenticated.SEC_FAULT_OUTPUT');
insert into topic_name (id) values ('unauthenticated.VES_MEASUREMENT_OUTPUT');
insert into topic_name (id) values ('EPC');
insert into topic_name (id) values ('HW');
-- in production, default enabled should be off
insert into topic(id, topic_name_id,enabled,save_raw,ttl_day,data_format) values (1, '_DL_DEFAULT_',B'1',B'0',3650,'JSON');
insert into topic(id, topic_name_id,correlate_cleared_message,enabled, message_id_path,data_format)
values (2, 'unauthenticated.SEC_FAULT_OUTPUT',B'1',B'1','/event/commonEventHeader/eventName,/event/commonEventHeader/reportingEntityName,/event/faultFields/specificProblem,/event/commonEventHeader/eventId','JSON');
insert into topic(id, topic_name_id,enabled, aggregate_array_path,flatten_array_path,data_format)
values (3, 'unauthenticated.VES_MEASUREMENT_OUTPUT',B'1',
'/event/measurementsForVfScalingFields/memoryUsageArray,/event/measurementsForVfScalingFields/diskUsageArray,/event/measurementsForVfScalingFields/cpuUsageArray,/event/measurementsForVfScalingFields/vNicPerformanceArray',
'/event/measurementsForVfScalingFields/astriMeasurement/astriDPMeasurementArray/astriInterface',
'JSON');
insert into topic(id, topic_name_id,enabled,  flatten_array_path,data_format)
values (4, 'EPC',B'1', '/event/measurementsForVfScalingFields/astriMeasurement/astriDPMeasurementArray/astriInterface', 'JSON');
insert into topic(id, topic_name_id,enabled, aggregate_array_path,data_format)
values (5, 'HW',B'1',
'/event/measurementsForVfScalingFields/memoryUsageArray,/event/measurementsForVfScalingFields/diskUsageArray,/event/measurementsForVfScalingFields/cpuUsageArray,/event/measurementsForVfScalingFields/vNicPerformanceArray',
'JSON');
insert into map_db_topic(db_id,topic_id) select db.id, topic.id from db_type, db, topic where db.db_type_id=db_type.id and db_type.tool=B'0';
insert into map_kafka_topic(kafka_id,topic_id) select kafka.id, topic.id from kafka, topic;
insert into design_type (id, name, db_type_id) values ('KIBANA_DB', 'Kibana Dashboard', 'KIBANA');
insert into design_type (id, name, db_type_id) values ('KIBANA_SEARCH', 'Kibana Search', 'KIBANA');
insert into design_type (id, name, db_type_id) values ('KIBANA_VISUAL', 'Kibana Visualization', 'KIBANA');
insert into design_type (id, name, db_type_id) values ('ES_MAPPING', 'Elasticsearch Field Mapping Template', 'ES');
insert into design_type (id, name, db_type_id) values ('DRUID_KAFKA_SPEC', 'Druid Kafka Indexing Service Supervisor Spec', 'DRUID');
insert into design (id, name,topic_name_id, submitted,body, design_type_id) values (1, 'Kibana Dashboard on EPC test1', 'EPC', B'0', 'body here', 'KIBANA_DB');
insert into map_db_design (design_id,db_id ) values (1, 6);
insert into data_exposure(id,note,sql_template,db_id) values ('totalBandwidth','KPI bandwidth history','select  from_unixtime(commonEventHeader.lastEpochMicrosec/1000) as timeStamp, sum(measurementFields.additionalFields."UPF.N3IncPkt._Dnn"+measurementFields.additionalFields."UPF.N3OgPkt._Dnn") as bandwidth from upf where commonEventHeader.sourceId = ''${id}'' and ( from_unixtime(commonEventHeader.lastEpochMicrosec/1000) between  from_iso8601_timestamp( ''${timeStamp}'') - interval ''${hour}'' hour  and from_iso8601_timestamp( ''${timeStamp}'') ) group by  commonEventHeader.lastEpochMicrosec order by commonEventHeader.lastEpochMicrosec desc ',3);
insert into data_exposure(id,note,sql_template,db_id) values ('totalTraffic','KPI sum over history','select commonEventHeader.sourceId as id,  sum(measurementFields.additionalFields."UPF.N3IncPkt._Dnn"+measurementFields.additionalFields."UPF.N3OgPkt._Dnn") as totalTraffic from upf where commonEventHeader.sourceId = ''${id}''  and  from_unixtime(commonEventHeader.lastEpochMicrosec/1000) <= from_iso8601_timestamp( ''${timeStamp}'') ',3);
insert into data_exposure(id,note,sql_template,db_id) values ('userNumber','KPI',' select  from_unixtime(commonEventHeader.lastEpochMicrosec/1000) as timeStamp, sum(measurementFields.additionalFields."AMF.RegSub._NS")  as userNumber from amf where commonEventHeader.sourceId = ''${id}'' and ( from_unixtime(commonEventHeader.lastEpochMicrosec/1000) between  from_iso8601_timestamp( ''${timeStamp}'') - interval ''${hour}'' hour  and from_iso8601_timestamp( ''${timeStamp}'') ) group by  commonEventHeader.lastEpochMicrosec, commonEventHeader.sourceId  order by commonEventHeader.lastEpochMicrosec   desc ',3);
