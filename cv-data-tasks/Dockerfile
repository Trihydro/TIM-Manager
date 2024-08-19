# Required resources for deployment:
# - cv-data-tasks-1.1.0-SNAPSHOT.jar
# - resdf.wyoroad.info.cer (from WyoCV resources)

FROM maven:3.8-eclipse-temurin-21-alpine

ENV install_dir /home/wyocv/wyocv_applications/cv-data-tasks

ADD . $install_dir

RUN keytool -import -alias resdf -storepass changeit -noprompt -cacerts -file ${install_dir}/resdf.wyoroad.info.cer

CMD java -jar ${install_dir}/cv-data-tasks-1.1.0-SNAPSHOT.jar
