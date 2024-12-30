FROM maven:3.8-eclipse-temurin-21-alpine

ADD . /home/timm/timm_applications/cv-data-controller

CMD java --add-opens java.base/java.lang=ALL-UNNAMED -jar /home/timm/timm_applications/cv-data-controller/cv-data-controller-1.4.0-SNAPSHOT.jar
