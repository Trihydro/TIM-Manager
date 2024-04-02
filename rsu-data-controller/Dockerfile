FROM ubuntu:latest

# Install Java
RUN apt-get update && apt install -y openjdk-21-jre

# Install snmpwalk
RUN apt-get install -y snmp

# Clean up
RUN apt-get autoremove -y \
    && apt-get clean -y

ADD rsu-data-controller-0.0.1-SNAPSHOT.jar /app/

CMD java -jar /app/rsu-data-controller-0.0.1-SNAPSHOT.jar