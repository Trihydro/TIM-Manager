FROM ubuntu:latest

ADD . /home/wyocv/wyocv_applications/logger-kafka-consumer

RUN apt-get update && \
 apt-get install -y openjdk-8-jdk

CMD java -jar /home/wyocv/wyocv_applications/logger-kafka-consumer/logger-kafka-consumer-0.0.1-SNAPSHOT.jar
