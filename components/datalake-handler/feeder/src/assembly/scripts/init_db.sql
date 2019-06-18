create database datalake;
use datalake;

CREATE TABLE `topic` (
  `name` varchar(255) NOT NULL,
  `correlate_cleared_message` bit(1) DEFAULT NULL,
  `enabled` bit(1) DEFAULT 0,
  `login` varchar(255) DEFAULT NULL,
  `message_id_path` varchar(255) DEFAULT NULL,
  `aggregate_array_path` varchar(2000) DEFAULT NULL,
  `flatten_array_path` varchar(2000) DEFAULT NULL,
  `pass` varchar(255) DEFAULT NULL,
  `save_raw` bit(1) DEFAULT NULL,
  `ttl` int(11) DEFAULT NULL,
  `data_format` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `db` (
  `name` varchar(255) NOT NULL,
  `enabled` bit(1) DEFAULT 0,
  `host` varchar(255) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  `database_name` varchar(255) DEFAULT NULL,
  `encrypt` bit(1) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `pass` varchar(255) DEFAULT NULL,
  `property1` varchar(255) DEFAULT NULL,
  `property2` varchar(255) DEFAULT NULL,
  `property3` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `map_db_topic` (
  `db_name` varchar(255) NOT NULL,
  `topic_name` varchar(255) NOT NULL,
  PRIMARY KEY (`db_name`,`topic_name`),
  KEY `FK_topic_name` (`topic_name`),
  CONSTRAINT `FK_topic_name` FOREIGN KEY (`topic_name`) REFERENCES `topic` (`name`),
  CONSTRAINT `FK_db_name` FOREIGN KEY (`db_name`) REFERENCES `db` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `portal` (
  `name` varchar(255) NOT NULL DEFAULT '',
  `enabled` bit(1) DEFAULT 0,
  `host` varchar(500) DEFAULT NULL,
  `port` int(5) unsigned DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `pass` varchar(255) DEFAULT NULL,
  `related_db` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`),
  KEY `FK_related_db` (`related_db`),
  CONSTRAINT `FK_related_db` FOREIGN KEY (`related_db`) REFERENCES `db` (`name`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `design_type` (
  `name` varchar(255) NOT NULL,
  `display` varchar(255) NOT NULL,
  `portal` varchar(255) DEFAULT NULL,
  `note` text DEFAULT NULL,
  PRIMARY KEY (`name`),
  KEY `FK_portal` (`portal`),
  CONSTRAINT `FK_portal` FOREIGN KEY (`portal`) REFERENCES `portal` (`name`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `portal_design` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `submitted` bit(1) DEFAULT 0,
  `body` text DEFAULT NULL,
  `note` text DEFAULT NULL,
  `topic` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_topic` (`topic`),
  KEY `FK_type` (`type`),
  CONSTRAINT `FK_topic` FOREIGN KEY (`topic`) REFERENCES `topic` (`name`) ON DELETE SET NULL,
  CONSTRAINT `FK_type` FOREIGN KEY (`type`) REFERENCES `design_type` (`name`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into db (`name`,`host`,`login`,`pass`,`database_name`) values ('Couchbase','dl_couchbase','dl','dl1234','datalake');
insert into db (`name`,`host`) values ('Elasticsearch','dl_es');
insert into db (`name`,`host`,`port`,`database_name`) values ('MongoDB','dl_mongodb',27017,'datalake');
insert into db (`name`,`host`) values ('Druid','dl_druid');
insert into db (`name`,`host`,`login`) values ('HDFS','dlhdfs','dl');


-- in production, default enabled should be off
insert into `topic`(`name`,`enabled`,`save_raw`,`ttl`,`data_format`) values ('_DL_DEFAULT_',1,0,3650,'JSON');
insert into `map_db_topic`(`db_name`,`topic_name`) select `name`, '_DL_DEFAULT_' from db;


insert into `topic`(`name`,correlate_cleared_message,`enabled`, message_id_path,`data_format`) values ('unauthenticated.SEC_FAULT_OUTPUT',1,1,'/event/commonEventHeader/eventName,/event/commonEventHeader/reportingEntityName,/event/faultFields/specificProblem,/event/commonEventHeader/eventId','JSON');
insert into `map_db_topic`(`db_name`,`topic_name`) select `name`, 'unauthenticated.SEC_FAULT_OUTPUT' from db;

insert into `topic`(`name`,`enabled`, aggregate_array_path,flatten_array_path,`data_format`) 
values ('unauthenticated.VES_MEASUREMENT_OUTPUT',1,
'/event/measurementsForVfScalingFields/memoryUsageArray,/event/measurementsForVfScalingFields/diskUsageArray,/event/measurementsForVfScalingFields/cpuUsageArray,/event/measurementsForVfScalingFields/vNicPerformanceArray',
'/event/measurementsForVfScalingFields/astriMeasurement/astriDPMeasurementArray/astriInterface',
'JSON');
insert into `map_db_topic`(`db_name`,`topic_name`) select `name`, 'unauthenticated.VES_MEASUREMENT_OUTPUT' from db;

insert into `topic`(`name`,`enabled`,  flatten_array_path,`data_format`) 
values ('EPC',1, 
'/event/measurementsForVfScalingFields/astriMeasurement/astriDPMeasurementArray/astriInterface',
'JSON');
insert into `map_db_topic`(`db_name`,`topic_name`) select `name`, 'EPC' from db;

insert into `topic`(`name`,`enabled`, aggregate_array_path,`data_format`) 
values ('HW',1,
'/event/measurementsForVfScalingFields/memoryUsageArray,/event/measurementsForVfScalingFields/diskUsageArray,/event/measurementsForVfScalingFields/cpuUsageArray,/event/measurementsForVfScalingFields/vNicPerformanceArray',
'JSON');
insert into `map_db_topic`(`db_name`,`topic_name`) select `name`, 'HW' from db;

insert into portal (`name`,`related_db`, host) values ('Kibana', 'Elasticsearch', 'dl_es');
insert into portal (`name`,`related_db`) values ('Elasticsearch', 'Elasticsearch');
insert into portal (`name`,`related_db`) values ('Druid', 'Druid');

insert into design_type (`name`,`display`,`portal`) values ('kibana_db', 'Kibana Dashboard', 'Kibana');
insert into design_type (`name`,`display`,`portal`) values ('kibana_search', 'Kibana Search', 'Kibana');
insert into design_type (`name`,`display`,`portal`) values ('kibana_visual', 'Kibana Visualization', 'Kibana');
insert into design_type (`name`,`display`,`portal`) values ('es_mapping', 'Elasticsearch Field Mapping Template', 'Elasticsearch');
insert into design_type (`name`,`display`,`portal`) values ('druid_kafka_spec', 'Druid Kafka Indexing Service Supervisor Spec', 'Druid');

