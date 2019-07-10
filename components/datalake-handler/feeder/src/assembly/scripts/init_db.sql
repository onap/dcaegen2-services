drop DATABASE datalake;
create database datalake;
use datalake;

CREATE TABLE `topic_name` (
  `id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `db_type` (
  `id` varchar(255) NOT NULL,
  `default_port` int(11) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `tool` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `db` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `database_name` varchar(255) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `encrypt` bit(1) DEFAULT NULL,
  `host` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pass` varchar(255) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  `property1` varchar(255) DEFAULT NULL,
  `property2` varchar(255) DEFAULT NULL,
  `property3` varchar(255) DEFAULT NULL,
  `db_type_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3njadtw43ieph7ftt4kxdhcko` (`db_type_id`),
  CONSTRAINT `FK3njadtw43ieph7ftt4kxdhcko` FOREIGN KEY (`db_type_id`) REFERENCES `db_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `portal` (
  `name` varchar(255) NOT NULL,
  `enabled` bit(1) DEFAULT NULL,
  `host` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `pass` varchar(255) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  `related_db` int(11) DEFAULT NULL,
  PRIMARY KEY (`name`),
  KEY `FKtl6e8ydm1k7k9r5ukv9j0bd0n` (`related_db`),
  CONSTRAINT `FKtl6e8ydm1k7k9r5ukv9j0bd0n` FOREIGN KEY (`related_db`) REFERENCES `db` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `design_type` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `db_type_id` varchar(255) NOT NULL,
  `portal` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm8rkv2qkq01gsmeq1c3y4w02x` (`db_type_id`),
  KEY `FKs2nspbhf5wv5d152l4j69yjhi` (`portal`),
  CONSTRAINT `FKm8rkv2qkq01gsmeq1c3y4w02x` FOREIGN KEY (`db_type_id`) REFERENCES `db_type` (`id`),
  CONSTRAINT `FKs2nspbhf5wv5d152l4j69yjhi` FOREIGN KEY (`portal`) REFERENCES `portal` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `design` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `body` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `submitted` bit(1) DEFAULT NULL,
  `design_type_id` varchar(255) NOT NULL,
  `topic_name_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo43yi6aputq6kwqqu8eqbspm5` (`design_type_id`),
  KEY `FKabb8e74230glxpaiai4aqsr34` (`topic_name_id`),
  CONSTRAINT `FKabb8e74230glxpaiai4aqsr34` FOREIGN KEY (`topic_name_id`) REFERENCES `topic_name` (`id`),
  CONSTRAINT `FKo43yi6aputq6kwqqu8eqbspm5` FOREIGN KEY (`design_type_id`) REFERENCES `design_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `kafka` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `broker_list` varchar(255) NOT NULL,
  `consumer_count` int(11) DEFAULT 3,
  `enabled` bit(1) NOT NULL,
  `excluded_topic` varchar(1023) DEFAULT '__consumer_offsets,__transaction_state',
  `group` varchar(255) DEFAULT 'datalake',
  `included_topic` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `pass` varchar(255) DEFAULT NULL,
  `secure` bit(1) DEFAULT b'0',
  `security_protocol` varchar(255) DEFAULT NULL,
  `timeout_sec` int(11) DEFAULT 10,
  `zk` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `topic` (
  `id` int(11) NOT NULL,
  `aggregate_array_path` varchar(255) DEFAULT NULL,
  `correlate_cleared_message` bit(1) NOT NULL DEFAULT b'0',
  `data_format` varchar(255) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `flatten_array_path` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `message_id_path` varchar(255) DEFAULT NULL,
  `pass` varchar(255) DEFAULT NULL,
  `save_raw` bit(1) NOT NULL DEFAULT b'0',
  `ttl_day` int(11) DEFAULT NULL,
  `topic_name_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj3pldlfaokdhqjfva8n3pkjca` (`topic_name_id`),
  CONSTRAINT `FKj3pldlfaokdhqjfva8n3pkjca` FOREIGN KEY (`topic_name_id`) REFERENCES `topic_name` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `map_db_design` (
  `design_id` int(11) NOT NULL,
  `db_id` int(11) NOT NULL,
  PRIMARY KEY (`design_id`,`db_id`),
  KEY `FKhpn49r94k05mancjtn301m2p0` (`db_id`),
  CONSTRAINT `FKfli240v96cfjbnmjqc0fvvd57` FOREIGN KEY (`design_id`) REFERENCES `design` (`id`),
  CONSTRAINT `FKhpn49r94k05mancjtn301m2p0` FOREIGN KEY (`db_id`) REFERENCES `db` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `map_db_topic` (
  `topic_id` int(11) NOT NULL,
  `db_id` int(11) NOT NULL,
  PRIMARY KEY (`db_id`,`topic_id`),
  KEY `FKq1jon185jnrr7dv1dd8214uw0` (`topic_id`),
  CONSTRAINT `FKirro29ojp7jmtqx9m1qxwixcc` FOREIGN KEY (`db_id`) REFERENCES `db` (`id`),
  CONSTRAINT `FKq1jon185jnrr7dv1dd8214uw0` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `map_kafka_topic` (
  `kafka_id` int(11) NOT NULL,
  `topic_id` int(11) NOT NULL,
  PRIMARY KEY (`topic_id`,`kafka_id`),
  KEY `FKtdrme4h7rxfh04u2i2wqu23g5` (`kafka_id`),
  CONSTRAINT `FK5q7jdxy54au5rcrhwa4a5igqi` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`id`),
  CONSTRAINT `FKtdrme4h7rxfh04u2i2wqu23g5` FOREIGN KEY (`kafka_id`) REFERENCES `kafka` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
