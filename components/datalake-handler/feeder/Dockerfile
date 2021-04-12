FROM nexus3.onap.org:10001/onap/integration-java11:8.0.0

LABEL maintainer="Guobiao Mo <guobiaomo@chinamobile.com>"

ARG user=datalake
ARG group=datalake

EXPOSE 1680

USER root
RUN addgroup $group && adduser --system --disabled-password --no-create-home --ingroup $group $user && \
    mkdir -p /datalake && \
    mkdir -p /datalake/db_init && \
    chown -R datalake:datalake /datalake


WORKDIR /datalake

#add the fat jar
COPY target/${JAR_FILE} .
COPY src/assembly/run.sh .
ADD src/assembly/init_db/scripts/db_init ./db_init/
COPY src/assembly/init_db/db_scripts/init_db.sql .
COPY src/assembly/init_db/db_scripts/init_db_data.sql .

RUN chmod -R 0755 ./* && \
    apk add --no-cache mariadb-client curl

USER datalake

CMD ["sh", "run.sh"]