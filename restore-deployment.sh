# This script assumes that you have the following directory: /home/wyocvadmin/wyocv
# which should contain directories with both a currently deployed .jar & Dockerfile and their backup.
# This also assumes that the directory name of each "submodule"
# was extracted from the name of the .jar that was deployed.
# For example, cv-data-tasks-X.X.X-SNAPSHOT.jar was deployed to cv-data-tasks/
#
# This script will:
# 1. Make the backup the currently deployed version by removing the .bak
# (e.g. cv-data-tasks-0.0.1-SNAPSHOT.jar.bak becomes cv-data-tasks-0.0.1-SNAPSHOT.jar)
# 2. Backup the currently deployed JAR by appending .bak to the original filename
# (e.g. cv-data-tasks-0.0.2-SNAPSHOT.jar becomes cv-data-tasks-0.0.2-SNAPSHOT.jar.bak)
# 3. Switch the current Dockerfile with the backup Dockerfile
# (e.g. Dockerfile.bak -> Dockerfile &  Dockerfile -> Dockerfile.bak)

directory=""
base=/home/wyocvadmin/wyocv
for path in $base/*; do
    directory=$(basename $path)

    # Skip the docker-compose.yml file
    if [ "$directory" = "docker-compose.yml" ]; then
        continue
    fi

    # Get current & backup JARs
    current_deployment_jar=$(ls $base/$directory/$directory-*.jar)
    backup_deployment_jar=$(ls $base/$directory/$directory-*.jar.bak)

    # Extract current & new versions from JARs (e.g. cv-data-tasks-X.X.X-SNAPSHOT.jar -> X.X.X-SNAPSHOT)
    current_deployment_version=$(echo $current_deployment_jar | grep -oP '(?<=-)\d.*(?=.jar)')
    backup_deployment_version=$(echo $backup_deployment_jar | grep -oP '(?<=-)\d.*(?=.jar)')

    # Backup the current deployment
    mv $current_deployment_jar current-deployment.jar.bak

    # 1. Make the backup the currently deployed version
    mv $backup_deployment_jar $base/$directory/$directory-$backup_deployment_version.jar

    # 2. Make the current deployment the backup
    mv current-deployment.jar.bak $base/$directory/$directory-$current_deployment_version.jar.bak

    # 3. Switch the current Dockerfile with the backup Dockerfile
    tempfile_dockerfile=$(mktemp)
    mv $base/$directory/Dockerfile.bak $tempfile_dockerfile
    mv $base/$directory/Dockerfile $base/$directory/Dockerfile.bak
    mv $tempfile_dockerfile $base/$directory/Dockerfile

    # inform the user of the changes
    echo "-------- $directory: $current_deployment_version -> $backup_deployment_version --------"
done