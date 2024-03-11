FROM maven:3.8-eclipse-temurin-21-alpine

# Install snmpwalk
RUN apk update && apk add net-snmp

# Clean up
RUN apk del maven

ADD rsu-data-controller-0.0.1-SNAPSHOT.jar /app/

CMD java -jar /app/rsu-data-controller-0.0.1-SNAPSHOT.jar