FROM ubuntu:latest

ADD . /home/wyocv/wyocv_applications/cv-data-tasks

RUN apt-get update && \
 apt-get install -y openjdk-8-jdk

CMD java -jar /home/wyocv/wyocv_applications/cv-data-tasks/cv-data-tasks-0.0.1-SNAPSHOT.jar
