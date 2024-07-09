#!/bin/bash

timestamp=$(date +'%d-%m-%YT%H.%M.%S')
backupsDir="/home/wyocvadmin/cvmanager/pgdb-backups"

# Move old backups to 'old' directory
mv $backupsDir/*.tar.gz $backupsDir/old

# Create new backup by tar-ing the volume inside a temporary container
sudo docker run -it -v jpo-cvmanager_pgdb:/cvmanager-pgdb -v $backupsDir:/pgdb-backups --name temp-pgdb-backup-helper ubuntu tar -czvf /pgdb-backups/pgdb-backup-$timestamp.tar.gz /cvmanager-pgdb

# Remove the temporary container
sudo docker rm temp-pgdb-backup-helper

# Inform the user that the backup has been saved
echo "A backup of the CV Manager PGSQL database has been saved to '$backupsDir'."