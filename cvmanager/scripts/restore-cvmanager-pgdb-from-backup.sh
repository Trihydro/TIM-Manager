#!/bin/bash

# This script restores the CV Manager PGSQL database from a backup file by removing the current database and
# restoring the backup inside a temporary container. The backup file should be specified as an argument to this script.

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
if [ -z "$DB_USER" ]; then
    echo "DB_USER is not set. Please set the DB_USER environment variable in the .env file."
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
if [ -z "$CVMANAGER_SOURCE_DIR" ]; then
    echo "CVMANAGER_SOURCE_DIR is not set. Please set the CVMANAGER_SOURCE_DIR environment variable in the .env file."
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

pathToPrimaryBackup=$1
pathToKeycloakBackup=$2

if [ -z "$pathToPrimaryBackup" ]; then
    echo "Error: no backup file specified. Usage: restore-cvmanager-pgdb-from-backup.sh <path-to-primary-backup> <path-to-keycloak-backup>"
    exit 1
fi

if [ -z "$pathToKeycloakBackup" ]; then
    echo "Error: no backup file specified. Usage: restore-cvmanager-pgdb-from-backup.sh <path-to-primary-backup> <path-to-keycloak-backup>"
    exit 1
fi

# Make sure the backup file exists
if [ ! -f $pathToPrimaryBackup ]; then
    echo "Error: backup file '$pathToPrimaryBackup' does not exist"
    exit 1
fi

if [ ! -f $pathToKeycloakBackup ]; then
    echo "Error: backup file '$pathToKeycloakBackup' does not exist"
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

# ask if user wants to attempt to create the schemas
read -p "Do you want to attempt to create the schemas? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    sudo psql -h $DB_HOST -U $DB_USER -d $DB_NAME -c "CREATE SCHEMA IF NOT EXISTS $PRIMARY_SCHEMA_NAME;"
        if [ $? -ne 0 ]; then
        echo "Something went wrong while creating the schemas. Restore cancelled."
        exit 1
    fi
    sudo psql -U $DB_USER -d $DB_NAME -c "CREATE SCHEMA IF NOT EXISTS $KEYCLOAK_SCHEMA_NAME;"
    if [ $? -ne 0 ]; then
        echo "Something went wrong while creating the schemas. Restore cancelled."
        exit 1
    fi
    echo "Schemas created."
elif [[ $REPLY =~ ^[Nn]$ ]]; then
    echo "Continuing without creating the schemas..."
else
    echo "Invalid input. Please enter 'y' or 'n'"
    exit 1
fi

# ask if user wants to create the tables
read -p "Do you want to create the tables for the primary schema? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    # run the create-tables script
    pathToTableScriptFromCvmanagerSourceDir="resources/sql_scripts/CVManager_CreateTables.sql"
    sudo psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f $CVMANAGER_SOURCE_DIR/$pathToTableScriptFromCvmanagerSourceDir
    if [ $? -ne 0 ]; then
        echo "Something went wrong while creating the tables. Restore cancelled."
        exit 1
    fi
    echo "Tables created."
elif [[ $REPLY =~ ^[Nn]$ ]]; then
    echo "Continuing without creating the tables..."
else
    echo "Invalid input. Please enter 'y' or 'n'"
    exit 1
fi

# Restore the backup by running pg_restore inside a temporary container
sudo docker rm temp-pgdb-restore-helper 2> /dev/null
echo "Enter postgres password to restore the primary schema:"
sudo docker run -it --rm -v $pathToPrimaryBackup:/pgdb-backup.dump --name temp-pgdb-restore-helper postgis/postgis:15-master pg_restore -n $PRIMARY_SCHEMA_NAME --clean --verbose -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME /pgdb-backup.dump
if [ $? -ne 0 ]; then
    sudo docker rm temp-pgdb-restore-helper 2> /dev/null
    echo "The pg_restore command returned an error status code. Please check the state of the database to determine if the restore was successful."
fi
echo "Enter postgres password to restore the keycloak schema:"
sudo docker run -it --rm -v $pathToKeycloakBackup:/pgdb-backup.dump --name temp-pgdb-restore-helper postgis/postgis:15-master pg_restore -n $KEYCLOAK_SCHEMA_NAME --clean --verbose -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME /pgdb-backup.dump
if [ $? -ne 0 ]; then
    sudo docker rm temp-pgdb-restore-helper 2> /dev/null
    echo "The pg_restore command returned an error status code. Please check the state of the database to determine if the restore was successful."
fi

# Inform the user that the backup has been restored
echo "The restore process has completed. Please check the state of the database to ensure that the restore was successful."