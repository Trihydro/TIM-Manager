#!/bin/bash

# This script restores the CV Manager PGSQL database from a backup file by removing the current database and
# restoring the backup inside a temporary container. The backup file should be specified as an argument to this script.

# Note: This script is intended to be run on the VM where the CV Manager is deployed.

# PGSQL info
db_name="postgres"
db_user="postgres"
db_host="10.145.7.48" # currently the IP address of the Test VM, which is where the CV Manager PGSQL database is deployed as a Docker container
db_port="5432"

cvmanagerSourceDir="/home/wyocvadmin/cvmanager/jpo-cvmanager"
pathToBackup=$1

if [ -z "$pathToBackup" ]; then
    echo "Error: no backup file specified"
    exit 1
fi

# Make sure the backup file exists
if [ ! -f $pathToBackup ]; then
    echo "Error: backup file '$pathToBackup' does not exist"
    exit 1
fi

# check that user wants to continue and inform them that the current database will be wiped
read -p "The current CV Manager database will be wiped. Do you want to continue? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Restore cancelled"
    exit 1
fi

# ask the user if they want to create a backup of the current database first
read -p "Do you want to create a backup of the current database before wiping it? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    # run the backup script
    echo "Running create-cvmanager-pgdb-backup.sh..."
    ./create-cvmanager-pgdb-backup.sh
    if [ $? -ne 0 ]; then
        echo "Something went wrong while creating the backup. Restore cancelled."
        exit 1
    fi
    echo "Backup created."
elif [[ $REPLY =~ ^[Nn]$ ]]; then
    echo "Continuing without creating a backup..."
else
    echo "Invalid input. Please enter 'y' or 'n'"
    exit 1
fi

# Remove the current database
sudo docker compose -f $cvmanagerSourceDir/docker-compose-addons.yml down
sudo docker volume rm jpo-cvmanager_pgdb
if [ $? -ne 0 ]; then
    echo "Error: failed to remove the current CV Manager PGSQL database"
    exit 1
fi
sudo docker compose -f $cvmanagerSourceDir/docker-compose.yml up -d cvmanager_postgres

# Wait for the database to start
echo "Waiting for the database to start..."
sleep 5

# Restore the backup by running pg_restore inside a temporary container
sudo docker rm temp-pgdb-restore-helper 2> /dev/null
sudo docker run -it -v jpo-cvmanager_pgdb:/cvmanager-pgdb -v $pathToBackup:/pgdb-backup.dump --name temp-pgdb-restore-helper postgis/postgis:15-master pg_restore --clean --verbose -U postgres -h $db_host -p $db_port -d $db_name /pgdb-backup.dump
if [ $? -ne 0 ]; then
    sudo docker rm temp-pgdb-restore-helper 2> /dev/null
    echo "The pg_restore command returned an error status code. Please check the state of the database to determine if the restore was successful."
    exit 1
fi

# Remove the temporary container
sudo docker rm temp-pgdb-restore-helper

# Inform the user that the backup has been restored
echo "The CV Manager PGSQL database has been restored from '$pathToBackup'."