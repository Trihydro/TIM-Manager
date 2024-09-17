#!/bin/bash

# This script creates a backup of the CV Manager PGSQL database by running pg_dump inside a temporary container.
# The backup is saved to a .dump file in the pgdb-backups directory.

# Note: This script is intended to be run on the VM where the CV Manager is deployed.

#  use current directory
working_dir=$(pwd)
echo "Working directory: $working_dir"

# if .env file exists, load it
if [ -f "$working_dir/.env" ]
then
    # if carriage returns are present, remove them
    sed -i 's/\r//g' $working_dir/.env

    echo "Loading environment variables from $working_dir/.env"
    export $(cat $working_dir/.env | sed 's/#.*//g' | xargs)
fi

if [ -z "$DB_NAME" ]; then
    echo "DB_NAME is not set. Please set the DB_NAME environment variable in the .env file."
    exit 1
fi
if [ -z "$DB_CVMANAGER_USER" ]; then
    echo "DB_CVMANAGER_USER is not set. Please set the DB_CVMANAGER_USER environment variable in the .env file."
    exit 1
fi
if [ -z "$DB_KEYCLOAK_USER" ]; then
    echo "DB_KEYCLOAK_USER is not set. Please set the DB_KEYCLOAK_USER environment variable in the .env file."
    exit 1
fi
if [ -z "$DB_HOST" ]; then
    echo "DB_HOST is not set. Please set the DB_HOST environment variable in the .env file."
    exit 1
fi
if [ -z "$DB_PORT" ]; then
    echo "DB_PORT is not set. Please set the DB_PORT environment variable in the .env file."
    exit 1
fi
if [ -z "$BACKUPS_DIR" ]; then
    echo "BACKUPS_DIR is not set. Please set the BACKUPS_DIR environment variable in the .env file."
    exit 1
fi
if [ -z "$PRIMARY_SCHEMA_NAME" ]; then
    echo "PRIMARY_SCHEMA_NAME is not set. Please set the PRIMARY_SCHEMA_NAME environment variable in the .env file."
    exit 1
fi
if [ -z "$KEYCLOAK_SCHEMA_NAME" ]; then
    echo "KEYCLOAK_SCHEMA_NAME is not set. Please set the KEYCLOAK_SCHEMA_NAME environment variable in the .env file."
    exit 1
fi

timestamp=$(date +'%d-%m-%YT%H.%M.%S')

# Move old backups to 'old' directory
mv $BACKUPS_DIR/*.dump $BACKUPS_DIR/old 2> /dev/null
if [ $? -ne 0 ]; then
    echo "No old backups found."
else
    echo "Old backups moved to '$BACKUPS_DIR/old'."
fi

# Create new backup by running pg_dump inside a temporary container and saving the output to a .dump file
docker rm temp-pgdb-backup-helper 2> /dev/null
if [ $? -ne 0 ]; then
    echo "No temporary container found."
else
    echo "Removed old temporary container."
fi
echo "Enter postgres password for user $DB_CVMANAGER_USER to backup the primary schema:"
docker run --rm -it -v $BACKUPS_DIR:/pgdb-backups --name temp-pgdb-backup-helper postgis/postgis:15-master pg_dump -n $PRIMARY_SCHEMA_NAME -U $DB_CVMANAGER_USER -h $DB_HOST -p $DB_PORT $DB_NAME -f /pgdb-backups/pgdb-primary-schema-backup-$timestamp.dump --format=custom
if [ $? -ne 0 ]; then
    sudo docker rm temp-pgdb-restore-helper 2> /dev/null
    echo "Failed to create a backup of the primary schema in the CV Manager PGSQL database."
    exit 1
fi
echo "Enter postgres password for user $DB_KEYCLOAK_USER to backup the keycloak schema:"
docker run --rm -it -v $BACKUPS_DIR:/pgdb-backups --name temp-pgdb-backup-helper postgis/postgis:15-master pg_dump -n $KEYCLOAK_SCHEMA_NAME -U $DB_KEYCLOAK_USER -h $DB_HOST -p $DB_PORT $DB_NAME -f /pgdb-backups/pgdb-keycloak-schema-backup-$timestamp.dump --format=custom
if [ $? -ne 0 ]; then
    sudo docker rm temp-pgdb-restore-helper 2> /dev/null
    echo "Failed to create a backup of the keycloak schema in the CV Manager PGSQL database."
    exit 1
fi

# Inform the user that the backup has been saved
echo "Backup of the primary schema saved to '$BACKUPS_DIR/pgdb-primary-schema-backup-$timestamp.dump'."
echo "Backup of the keycloak schema saved to '$BACKUPS_DIR/pgdb-keycloak-schema-backup-$timestamp.dump'."