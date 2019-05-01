create database datalake;
use datalake;

CREATE TABLE `topic` (
  `name` varchar(255) NOT NULL,
  `correlate_cleared_message` bit(1) DEFAULT NULL,
  `enabled` bit(1) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `message_id_path` varchar(255) DEFAULT NULL,
  `pass` varchar(255) DEFAULT NULL,
  `save_raw` bit(1) DEFAULT NULL,
  `ttl` int(11) DEFAULT NULL,
  `data_format` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `db` (
  `name` varchar(255) NOT NULL,
  `enabled` bit(1) DEFAULT NULL,
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

CREATE TABLE `dashboard_template` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `body` text DEFAULT NULL,
  `note` text DEFAULT NULL,
  `topic` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_topic` (`topic`),
  KEY `FK_type` (`type`),
  CONSTRAINT `FK_topic` FOREIGN KEY (`topic`) REFERENCES `topic` (`name`) ON DELETE SET NULL,
  CONSTRAINT `FK_type` FOREIGN KEY (`type`) REFERENCES `dashboard_type` (`name`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dashboard_type` (
  `name` varchar(255) NOT NULL,
  `dashboard` varchar(255) DEFAULT NULL,
  `note` text DEFAULT NULL,
  PRIMARY KEY (`name`),
  KEY `FK_dashboard` (`dashboard`),
  CONSTRAINT `FK_dashboard` FOREIGN KEY (`dashboard`) REFERENCES `dashboard` (`name`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dashboard` (
  `name` varchar(255) NOT NULL DEFAULT '',
  `type` varchar(255) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `port` int(5) unsigned DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `related_db` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`),
  KEY `FK_related_db` (`related_db`),
  CONSTRAINT `FK_related_db` FOREIGN KEY (`related_db`) REFERENCES `db` (`name`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



insert into db (`name`,`host`,`login`,`pass`,`database_name`) values ('Couchbase','dl_couchbase','dl','dl1234','datalake');
insert into db (`name`,`host`) values ('Elasticsearch','dl_es');
insert into db (`name`,`host`,`port`,`database_name`) values ('MongoDB','dl_mongodb',27017,'datalake');
insert into db (`name`,`host`) values ('Druid','dl_druid');


-- in production, default enabled should be off
insert into `topic`(`name`,`enabled`,`save_raw`,`ttl`,`data_format`) values ('_DL_DEFAULT_',1,0,3650,'JSON');
insert into `topic`(`name`,`enabled`) values ('__consumer_offsets',0);
insert into `topic`(`name`,correlate_cleared_message,`enabled`, message_id_path) values ('unauthenticated.SEC_FAULT_OUTPUT',1,0,'/event/commonEventHeader/eventName,/event/commonEventHeader/reportingEntityName,/event/faultFields/specificProblem');


insert into `map_db_topic`(`db_name`,`topic_name`) values ('Couchbase','_DL_DEFAULT_');
insert into `map_db_topic`(`db_name`,`topic_name`) values ('Elasticsearch','_DL_DEFAULT_');
insert into `map_db_topic`(`db_name`,`topic_name`) values ('MongoDB','_DL_DEFAULT_');
insert into `map_db_topic`(`db_name`,`topic_name`) values ('Druid','_DL_DEFAULT_');
