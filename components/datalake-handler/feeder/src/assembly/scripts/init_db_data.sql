INSERT INTO datalake.kafka(
   id
  ,name
  ,check_topic_interval_sec
  ,consumer_count
  ,enabled
  ,excluded_topic
  ,`group`
  ,broker_list
  ,included_topic
  ,login
  ,pass
  ,secure
  ,security_protocol
  ,timeout_sec
  ,zk
) VALUES (
  'KAFKA_1'
  ,'main Kafka cluster' -- name - IN varchar(255)
  ,10   -- check_topic_sec - IN int(11)
  ,3   -- consumer_count - IN int(11)
  ,1   -- enabled - IN bit(1)
  ,''  -- excluded_topic - IN varchar(255)
  ,'dlgroup'  -- group - IN varchar(255)
  ,'message-router-kafka:9092'  -- host_port - IN varchar(255)
  ,''  -- included_topic - IN varchar(255)
  ,'admin'  -- login - IN varchar(255)
  ,'admin-secret'  -- pass - IN varchar(255)
  ,0   -- secure - IN bit(1)
  ,'SASL_PLAINTEXT'  -- security_protocol - IN varchar(255)
  ,10   -- timeout_sec - IN int(11)
  ,'message-router-zookeeper:2181'  -- zk - IN varchar(255)
);

insert into db_type (`id`, `name`, tool) values ('CB', 'Couchbase', false);
insert into db_type (`id`, `name`, tool) values ('ES', 'Elasticsearch', false);
insert into db_type (`id`, `name`, tool,`default_port`) values ('MONGO', 'MongoDB', false, 27017);
insert into db_type (`id`, `name`, tool) values ('DRUID', 'Druid', false);
insert into db_type (`id`, `name`, tool) values ('HDFS', 'HDFS', false);
insert into db_type (`id`, `name`, tool) values ('KIBANA', 'Kibana', true);
insert into db_type (`id`, `name`, tool) values ('SUPERSET', 'Apache Superset', true);

insert into db (id, db_type_id, enabled, `name`,`host`,`login`,`pass`,`database_name`) values (1, 'CB', true, 'Couchbase 1','dl-couchbase','dl','dl1234','datalake');
insert into db (id, db_type_id, enabled, `name`,`host`) values (2, 'ES', true, 'Elasticsearch','dl-es');
insert into db (id, db_type_id, enabled, `name`,`host`,`port`,`database_name`) values (3, 'MONGO', true, 'MongoDB 1','dl-mongodb',27017,'datalake');
insert into db (id, db_type_id, enabled, `name`,`host`) values (4, 'DRUID', true, 'Druid','dl-druid');
insert into db (id, db_type_id, enabled, `name`,`host`,`login`) values (5, 'HDFS', true, 'Hadoop Cluster','dl-hdfs','dl');
insert into db (id, db_type_id, enabled, `name`,`host`) values (6, 'KIBANA', true, 'Kibana demo','dl-es');
insert into db (id, db_type_id, enabled, `name`,`host`) values (7, 'SUPERSET', true, 'Superset demo','dl-druid');


insert into topic_name (id) values ('_DL_DEFAULT_');
insert into topic_name (id) values ('unauthenticated.SEC_FAULT_OUTPUT');
insert into topic_name (id) values ('unauthenticated.VES_MEASUREMENT_OUTPUT');
insert into topic_name (id) values ('EPC');
insert into topic_name (id) values ('HW');

-- in production, default enabled should be off
insert into `topic`(id, `topic_name_id`,`enabled`,`save_raw`,`ttl_day`,`data_format`) values (1, '_DL_DEFAULT_',1,0,3650,'JSON');

insert into `topic`(id, `topic_name_id`,correlate_cleared_message,`enabled`, message_id_path,`data_format`) 
values (2, 'unauthenticated.SEC_FAULT_OUTPUT',1,1,'/event/commonEventHeader/eventName,/event/commonEventHeader/reportingEntityName,/event/faultFields/specificProblem,/event/commonEventHeader/eventId','JSON');

insert into `topic`(id, `topic_name_id`,`enabled`, aggregate_array_path,flatten_array_path,`data_format`) 
values (3, 'unauthenticated.VES_MEASUREMENT_OUTPUT',1,
'/event/measurementsForVfScalingFields/memoryUsageArray,/event/measurementsForVfScalingFields/diskUsageArray,/event/measurementsForVfScalingFields/cpuUsageArray,/event/measurementsForVfScalingFields/vNicPerformanceArray',
'/event/measurementsForVfScalingFields/astriMeasurement/astriDPMeasurementArray/astriInterface',
'JSON');

insert into `topic`(id, `topic_name_id`,`enabled`,  flatten_array_path,`data_format`) 
values (4, 'EPC',1, '/event/measurementsForVfScalingFields/astriMeasurement/astriDPMeasurementArray/astriInterface', 'JSON');

insert into `topic`(id, `topic_name_id`,`enabled`, aggregate_array_path,`data_format`) 
values (5, 'HW',1,
'/event/measurementsForVfScalingFields/memoryUsageArray,/event/measurementsForVfScalingFields/diskUsageArray,/event/measurementsForVfScalingFields/cpuUsageArray,/event/measurementsForVfScalingFields/vNicPerformanceArray',
'JSON'); 


insert into `map_db_topic`(`db_id`,`topic_id`) select db.id, topic.id from db_type, db, topic where db.db_type_id=db_type.id and db_type.tool=0;
insert into `map_kafka_topic`(`kafka_id`,`topic_id`) select kafka.id, topic.id from kafka, topic;


insert into design_type (id, `name`, `db_type_id`) values ('KIBANA_DB', 'Kibana Dashboard', 'KIBANA');
insert into design_type (id, `name`, `db_type_id`) values ('KIBANA_SEARCH', 'Kibana Search', 'KIBANA');
insert into design_type (id, `name`, `db_type_id`) values ('KIBANA_VISUAL', 'Kibana Visualization', 'KIBANA');
insert into design_type (id, `name`, `db_type_id`) values ('ES_MAPPING', 'Elasticsearch Field Mapping Template', 'ES');
insert into design_type (id, `name`, `db_type_id`) values ('DRUID_KAFKA_SPEC', 'Druid Kafka Indexing Service Supervisor Spec', 'DRUID');


insert into design (id, `name`,topic_name_id, `submitted`,`body`, design_type_id) values (1, 'Kibana Dashboard on EPC test1', 'EPC',  0, 'body here', 'KIBANA_DB');

insert into map_db_design (`design_id`,`db_id` ) values (1, 6);
