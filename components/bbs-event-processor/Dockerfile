FROM nexus3.onap.org:10001/onap/integration-java11:8.0.0

ARG PROJECT_BUILD_DIR_NAME
ARG FINAL_JAR
ARG DEPENDENCIES_DIR
ARG DOCKER_ARTIFACT_DIR
ARG user=bbs-ep
ARG group=bbs-ep


#Add a new user and group to allow container to be run as non-root
USER root
RUN addgroup $group && adduser --system --disabled-password --no-create-home --ingroup $group $user

#Copy dependencies and executable jar
WORKDIR ${DOCKER_ARTIFACT_DIR}
COPY --chown=${user}:${group} ${PROJECT_BUILD_DIR_NAME}/${FINAL_JAR} .
COPY --chown=${user}:${group} KeyStore.jks .
COPY --chown=${user}:${group} KeyStorePass.txt .

#Overcome Docker limitation to put ARG inside ENTRYPOINT
RUN ln -s ${FINAL_JAR} bbs-ep.jar
COPY --chown=${user}:${group} ${PROJECT_BUILD_DIR_NAME}/${DEPENDENCIES_DIR} ./${DEPENDENCIES_DIR}

EXPOSE 8100

USER ${user}
ENTRYPOINT ["java", "-jar", "bbs-ep.jar"]
