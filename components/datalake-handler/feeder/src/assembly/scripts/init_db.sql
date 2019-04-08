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
  `default_topic` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`),
  KEY `FK_default_topic` (`default_topic`),
  CONSTRAINT `FK_default_topic` FOREIGN KEY (`default_topic`) REFERENCES `topic` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `db` (
  `name` varchar(255) NOT NULL,
  `host` varchar(255) DEFAULT NULL,
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


insert into db (name,host,login,pass,property1) values ('Couchbase','dl_couchbase','dmaap','dmaap1234','dmaap');
insert into db (name,host) values ('Elasticsearch','dl_es');
insert into db (name,host) values ('MongoDB','dl_mongodb');
insert into db (name,host) values ('Druid','dl_druid');


-- in production, default enabled should be off
insert into `topic`(`name`,`enabled`,`save_raw`,`ttl`,`data_format`) values ('_DL_DEFAULT_',1,0,3650,'JSON');
insert into `topic`(`name`,`enabled`) values ('__consumer_offsets',0);


insert into `map_db_topic`(`db_name`,`topic_name`) values ('MongoDB','_DL_DEFAULT_'); 
