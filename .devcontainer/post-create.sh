WYOCV_RES_DIR=/workspaces/WyoCV/resources

# Install jpo-ode JARs to maven repository
mvn install:install-file -Dfile="${WYOCV_RES_DIR}/jpo-ode-plugins-1.1.0.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-plugins -Dversion="1.1.0" -Dpackaging=jar
mvn install:install-file -Dfile="${WYOCV_RES_DIR}/jpo-ode-core-1.1.0.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-core -Dversion="1.1.0" -Dpackaging=jar
mvn install:install-file -Dfile="${WYOCV_RES_DIR}/jpo-ode-common-1.1.0.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-common -Dversion="1.1.0" -Dpackaging=jar
mvn install:install-file -Dfile="${WYOCV_RES_DIR}/jpo-ode-svcs.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-svcs -Dversion="1.1.0" -Dpackaging=jar
mvn install:install-file -Dfile="${WYOCV_RES_DIR}/ojdbc8.jar" -DgroupId="com.oracle" -DartifactId=ojdbc8 -Dversion="12.2.0.1.0" -Dpackaging=jar
mvn install:install-file -Dfile="${WYOCV_RES_DIR}/ucp.jar" -DgroupId="com.oracle" -DartifactId=ucp -Dversion="12.2.0.1.0" -Dpackaging=jar

# Starting in vscode 1.42.0, there appears to be an issue w/ git authentication
# Workaround until https://github.com/microsoft/vscode-remote-release/issues/2340 is resolved:
ln -s "$(ls ~/.vscode-server/bin/* -dt | head -1)/node" /usr/local/bin

# Install resdf.wyoroad.info's SSL certificate
keytool -import -alias resdf -storepass changeit -noprompt -trustcacerts -keystore /usr/lib/jvm/java-11-openjdk-amd64/jre/lib/security/cacerts -file ${WYOCV_RES_DIR}/resdf.wyoroad.info.cer