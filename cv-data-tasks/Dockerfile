# Required resources for deployment:
# - cv-data-tasks-0.0.1-SNAPSHOT.jar
# - resdf.wyoroad.info.cer (from WyoCV resources)

FROM ubuntu:latest

ENV install_dir /home/wyocv/wyocv_applications/cv-data-tasks

ADD . $install_dir

RUN apt-get update && \
 apt-get install -y openjdk-8-jdk

RUN keytool -import -alias resdf -storepass changeit -noprompt -trustcacerts -keystore /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts -file ${install_dir}/resdf.wyoroad.info.cer

CMD java -jar ${install_dir}/cv-data-tasks-0.0.1-SNAPSHOT.jar
