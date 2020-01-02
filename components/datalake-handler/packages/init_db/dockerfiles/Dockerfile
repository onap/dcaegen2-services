FROM ubuntu:16.04 AS builder
MAINTAINER Kate Hsuan <kate.hsuan@qct.io>

LABEL org.label-schema.docker.build="docker build -t dl-db-init . --no-cache" \
      org.label-schema.docker.run="docker run dl-init"

WORKDIR /app
WORKDIR /app/db_init
ADD packages/init_db/scripts/db_init/ .
RUN chmod 0755 ./*
WORKDIR /app
COPY packages/init_db/scripts/run.sh .
COPY feeder/src/assembly/scripts/init_db.sql .
COPY feeder/src/assembly/scripts/init_db_data.sql .

RUN chmod 0755 /app/run.sh

ARG mysqlpasswd="0000"
ENV MYSQL_ROOT_PASSWORD=$mysqlpasswd

ARG mysqlhost="192.168.87.7"
ENV MYSQL_HOST=$mysqlhost

ARG mysqlport="3306"
ENV MYSQL_PORT=${mysqlport}

RUN apt update && \
    apt install -y mariadb-client-core-10.0

CMD ["sh", "run.sh"]
