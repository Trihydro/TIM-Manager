FROM ubuntu:latest

ADD . /home/wyocv/wyocv_applications/ode-mongo-logger

RUN apt-get update && \
 apt-get install -y openjdk-8-jdk

CMD java -jar /home/wyocv/wyocv_applications/ode-mongo-logger/ode-mongo-logger-0.0.1-SNAPSHOT.jar
