# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.2.0] - 2026/04/03

- [DCAEGEN2-3433] Migrate datalake-feeder from message-router/ZooKeeper to Kafka AdminClient
- [DCAEGEN2-3433] Move Kafka connection config from database to application.properties
- [DCAEGEN2-3433] Rename DmaapService to KafkaAdminService
- [DCAEGEN2-3433] Update SQL init scripts for Strimzi Kafka bootstrap servers

## [1.1.3] - 2026/04/02

- Modernise base Docker image to eclipse-temurin:11-jre-alpine

## [1.1.2]

### Changed

- CodeCoverage improvement for dcaegen2-services-data-handler (DCAEGEN2-3161)

## [1.1.1] 2022-09-14

- DCAEGEN2-3004 - Fix DL-Admin Docker docker build issue

## [1.0.0] 2019-08-01

### Initial DL-Admin UI code

### Features

- add button component ([9e04f08](https://gerrit.onap.org/r/dcaegen2/services/commits/9e04f08))
- add database to design module ([eba414f](https://gerrit.onap.org/r/dcaegen2/services/commits/eba414f))
- change the structure of the project ([f761909](https://gerrit.onap.org/r/dcaegen2/services/commits/f761909))
- new changes for design module ([c4c391b](https://gerrit.onap.org/r/dcaegen2/services/commits/c4c391b))
