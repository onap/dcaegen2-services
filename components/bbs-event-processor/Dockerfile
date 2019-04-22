FROM openjdk:8-jre-alpine

ARG PROJECT_BUILD_DIR_NAME
ARG FINAL_JAR
ARG DEPENDENCIES_DIR
ARG DOCKER_ARTIFACT_DIR

#Add a new user and group to allow container to be run as non-root
RUN addgroup -S bbs-ep && adduser -S -G bbs-ep bbs-ep

#Copy dependencies and executable jar
WORKDIR ${DOCKER_ARTIFACT_DIR}
COPY ${PROJECT_BUILD_DIR_NAME}/${FINAL_JAR} .
#Overcome Docker limitation to put ARG inside ENTRYPOINT
RUN ln -s ${FINAL_JAR} bbs-ep.jar
COPY ${PROJECT_BUILD_DIR_NAME}/${DEPENDENCIES_DIR} ./${DEPENDENCIES_DIR}

EXPOSE 8100

USER bbs-ep:bbs-ep
ENTRYPOINT ["java", "-jar", "bbs-ep.jar"]
