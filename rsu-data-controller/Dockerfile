FROM ubuntu:latest

RUN apt-get update && \
 apt-get install -y openjdk-8-jdk

ADD rsu-data-controller-0.0.1-SNAPSHOT.jar /app/

CMD java -jar /app/rsu-data-controller-0.0.1-SNAPSHOT.jar