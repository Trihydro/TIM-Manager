FROM ubuntu:latest

ADD . /home/wyocv/wyocv_applications/tim-refresh

RUN apt-get update && \
 apt-get install -y openjdk-8-jdk

CMD java -jar /home/wyocv/wyocv_applications/tim-refresh/tim-refresh-0.0.1-SNAPSHOT.jar
