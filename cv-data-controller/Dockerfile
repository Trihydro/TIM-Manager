FROM ubuntu:latest

RUN apt-get update && \
 apt-get install -y openjdk-11-jre

ADD . /home/wyocv/wyocv_applications/cv-data-controller

CMD java -jar /home/wyocv/wyocv_applications/cv-data-controller/cv-data-controller-0.0.1-SNAPSHOT.jar
