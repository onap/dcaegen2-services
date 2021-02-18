FROM nexus3.onap.org:10001/onap/integration-java11:latest

MAINTAINER Guobiao Mo <guobiaomo@chinamobile.com>

EXPOSE 1680

USER root
RUN groupadd -r datalake && useradd -r -g datalake datalake && \
    mkdir /home/datalake

WORKDIR /home/datalake

#add the fat jar
COPY target/${JAR_FILE} /home/datalake/
COPY src/assembly/run.sh /home/datalake/

WORKDIR /home/datalake/db_init
ADD src/assembly/init_db/scripts/db_init .

RUN chmod 0755 ./*
WORKDIR /home/datalake
COPY src/assembly/init_db/db_scripts/init_db.sql .
COPY src/assembly/init_db/db_scripts/init_db_data.sql .

RUN chmod -R 0755 ./* && \
    chown -R datalake:datalake /home/datalake 

RUN apk install --no-cache mariadb curl

USER datalake

CMD ["sh", "run.sh"]

