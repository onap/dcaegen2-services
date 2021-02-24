FROM nexus3.onap.org:10001/onap/integration-java11:8.0.0

MAINTAINER Guobiao Mo <guobiaomo@chinamobile.com>

EXPOSE 1680

USER root

RUN addgroup datalake && adduser -G datalake -h /home/datalake -D datalake
RUN mkdir /home/datalake/db_init

WORKDIR /home/datalake

#add the fat jar
COPY target/${JAR_FILE} /home/datalake/
COPY src/assembly/run.sh /home/datalake/

WORKDIR /home/datalake/db_init
ADD src/assembly/init_db/scripts/db_init .
WORKDIR /home/datalake
COPY src/assembly/init_db/db_scripts/init_db.sql .
COPY src/assembly/init_db/db_scripts/init_db_data.sql .

RUN chmod -R 0755 ./* && \
    chown -R datalake:datalake /home/datalake

RUN apk --update add postgresql-client curl

USER datalake

ENTRYPOINT  /home/datalake/run.sh
