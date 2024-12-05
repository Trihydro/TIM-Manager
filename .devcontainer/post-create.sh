WYOCV_RES_DIR=/workspaces/WyoCV/resources

# Install jpo-ode JARs to maven repository
mvn install:install-file -Dfile="${WYOCV_RES_DIR}/jpo-ode-plugins-2.0.0-SNAPSHOT.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-plugins -Dversion="2.0.0-SNAPSHOT" -Dpackaging=jar
mvn install:install-file -Dfile="${WYOCV_RES_DIR}/jpo-ode-core-2.0.0-SNAPSHOT.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-core -Dversion="2.0.0-SNAPSHOT" -Dpackaging=jar
mvn install:install-file -Dfile="${WYOCV_RES_DIR}/jpo-ode-common-2.0.0-SNAPSHOT.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-common -Dversion="2.0.0-SNAPSHOT" -Dpackaging=jar
mvn install:install-file -Dfile="${WYOCV_RES_DIR}/jpo-ode-svcs.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-svcs -Dversion="2.0.0-SNAPSHOT" -Dpackaging=jar
mvn install:install-file -Dfile="${WYOCV_RES_DIR}/ucp.jar" -DgroupId="com.oracle" -DartifactId=ucp -Dversion="12.2.0.1.0" -Dpackaging=jar
mvn install:install-file -Dfile="${WYOCV_RES_DIR}/postgresql-42.6.0.jar" -DgroupId="org.postgresql" -DartifactId=postgresql -Dversion="42.6.0" -Dpackaging=jar

# Starting in vscode 1.42.0, there appears to be an issue w/ git authentication
# Workaround until https://github.com/microsoft/vscode-remote-release/issues/2340 is resolved:
ln -s "$(ls ~/.vscode-server/bin/* -dt | head -1)/node" /usr/local/bin

# Uncomment and modify the certificate name on the following line to import a certificate into the Java keystore
# keytool -import -alias resdf -storepass changeit -noprompt -cacerts -file ${WYOCV_RES_DIR}/<certificate-name>.cer