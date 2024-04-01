FROM maven:3.8-eclipse-temurin-21-alpine

ADD . /home/wyocv/wyocv_applications/cv-data-controller

CMD java --add-opens java.base/java.lang=ALL-UNNAMED -jar /home/wyocv/wyocv_applications/cv-data-controller/cv-data-controller-0.0.2-SNAPSHOT.jar
