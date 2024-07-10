#!/bin/bash

# This script creates a backup of the CV Manager PGSQL database by running pg_dump inside a temporary container.
# The backup is saved to a .dump file in the pgdb-backups directory.

# Note: This script is intended to be run on the VM where the CV Manager is deployed.

timestamp=$(date +'%d-%m-%YT%H.%M.%S')
backupsDir="/home/wyocvadmin/cvmanager/pgdb-backups"

# PGSQL info
db_name="postgres"
db_user="postgres"
db_host="10.145.7.48"
db_port="5432"

# Move old backups to 'old' directory
mv $backupsDir/*.dump $backupsDir/old

# Create new backup by running pg_dump inside a temporary container and saving the output to a .dump file
docker run -it -v jpo-cvmanager_pgdb:/cvmanager-pgdb -v $backupsDir:/pgdb-backups --name temp-pgdb-backup-helper postgis/postgis:15-master pg_dump -U postgres -h $db_host -p $db_port $db_name -f /pgdb-backups/pgdb-backup-$timestamp.dump --format=custom
if [ $? -ne 0 ]; then
    echo "Failed to create a backup of the CV Manager PGSQL database."
    exit 1
fi

# Remove the temporary container
docker rm temp-pgdb-backup-helper

# Inform the user that the backup has been saved
echo "A backup of the CV Manager PGSQL database has been saved to '$backupsDir/pgdb-backup-$timestamp.dump'."