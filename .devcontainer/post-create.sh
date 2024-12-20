# Please note that the following must be set to the name of the
# repository for the devcontainer to build and run correctly
REPO_PATH=$(dirname $(dirname $(realpath $0)))
REPO_NAME=$(basename ${REPO_PATH})
TIMM_RES_DIR=/workspaces/${REPO_NAME}/resources

echo "TIMM_RES_DIR: ${TIMM_RES_DIR}"

# Install UCP and Postgres to the local Maven repository
mvn install:install-file -Dfile="${TIMM_RES_DIR}/ucp.jar" -DgroupId="com.oracle" -DartifactId=ucp -Dversion="12.2.0.1.0" -Dpackaging=jar
mvn install:install-file -Dfile="${TIMM_RES_DIR}/postgresql-42.6.0.jar" -DgroupId="org.postgresql" -DartifactId=postgresql -Dversion="42.6.0" -Dpackaging=jar

# Install python
apt update
apt install -y python3 python3-pip

# Starting in vscode 1.42.0, there appears to be an issue w/ git authentication
# Workaround until https://github.com/microsoft/vscode-remote-release/issues/2340 is resolved:
ln -s "$(ls ~/.vscode-server/bin/* -dt | head -1)/node" /usr/local/bin

# Uncomment and modify the certificate name on the following line to import a certificate into the Java keystore
# keytool -import -alias resdf -storepass changeit -noprompt -cacerts -file ${TIMM_RES_DIR}/<certificate-name>.cer