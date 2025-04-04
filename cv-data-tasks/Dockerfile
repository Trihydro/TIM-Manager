# Required resources for deployment:
# - cv-data-tasks-2.0.0.jar

FROM maven:3.8-eclipse-temurin-21-alpine

ENV install_dir /home/timm/timm_applications/cv-data-tasks

ADD . $install_dir

# Uncomment and modify the certificate name on the following line to import a certificate into the Java keystore
# RUN keytool -import -alias resdf -storepass changeit -noprompt -cacerts -file ${install_dir}/<certificate-name>.cer

CMD java -jar ${install_dir}/cv-data-tasks-2.0.0.jar
