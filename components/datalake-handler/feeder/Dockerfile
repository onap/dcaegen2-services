FROM openjdk:8-jre-slim

MAINTAINER Guobiao Mo <guobiaomo@chinamobile.com>

EXPOSE 1680

RUN groupadd -r datalake && useradd -r -g datalake datalake
RUN mkdir /home/datalake

USER datalake
 
WORKDIR /home/datalake

#add the fat jar
COPY target/${JAR_FILE} /home/datalake/
COPY src/assembly/run.sh /home/datalake/

CMD ["sh", "run.sh"]

