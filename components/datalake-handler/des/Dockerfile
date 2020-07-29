FROM openjdk:11-jre-slim

MAINTAINER Guobiao Mo <guobiaomo@chinamobile.com>

EXPOSE 1681

RUN groupadd -r datalake && useradd -r -g datalake datalake
RUN mkdir /home/datalake/

USER datalake
 
WORKDIR /home/datalake

#add the fat jar
COPY target/${JAR_FILE} /home/datalake/
COPY src/assembly/run.sh /home/datalake/

WORKDIR /home/datalake/db_init
ADD src/assembly/init_db/scripts/db_init .
USER root
RUN chmod 0755 ./*
WORKDIR /home/datalake
COPY src/assembly/init_db/scripts/init_db.sql .
COPY src/assembly/init_db/scripts/init_db_data.sql .

RUN apt update && \
    apt install -y mariadb-client && \
    apt install -y curl

USER datalake

CMD ["sh", "run.sh"]

