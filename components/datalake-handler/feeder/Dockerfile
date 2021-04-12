FROM nexus3.onap.org:10001/onap/integration-java11:8.0.0

LABEL maintainer="Guobiao Mo <guobiaomo@chinamobile.com>"

ARG user=datalake
ARG group=datalake

EXPOSE 1680

USER root

WORKDIR /datalake
RUN addgroup $group && adduser --system --shell /bin/false --disabled-password --no-create-home --ingroup $group $user && \
    chown -R $user:$group /datalake && \
    chmod g+s /datalake && \
    mkdir -p /datalake/db_init

#add the fat jar
COPY --chown=$user:$group target/${JAR_FILE} .
COPY --chown=$user:$group src/assembly/run.sh .
COPY --chown=$user:$group src/assembly/init_db/scripts/db_init ./db_init/
COPY --chown=$user:$group src/assembly/init_db/db_scripts/init_db.sql .
COPY --chown=$user:$group src/assembly/init_db/db_scripts/init_db_data.sql .

RUN chmod -R 0755 *.sql && \
    chmod u+x run.sh && \
    apk add --no-cache mariadb-client curl

USER $user
ENTRYPOINT ["/bin/sh"]
CMD ["run.sh"]
