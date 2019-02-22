FROM openjdk:8-jre

MAINTAINER Guobiao Mo <guobiaomo@chinamobile.com>

EXPOSE 1680

RUN mkdir /home/datalake
WORKDIR /home/datalake

#add the fat jar
COPY target/feeder-1.0.jar /home/datalake/
COPY src/assembly/run.sh /home/datalake/

CMD ["sh", "run.sh"]

