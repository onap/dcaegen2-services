FROM openjdk:8-jre

MAINTAINER Guobiao Mo <guobiaomo@chinamobile.com>

EXPOSE 1680

RUN mkdir /home/datalake
WORKDIR /home/datalake

#add the fat jar
COPY target/feeder-1.0.0-SNAPSHOT.jar /home/datalake/
COPY src/assembly/run.sh /home/datalake/

CMD ["sh", "run.sh"]

