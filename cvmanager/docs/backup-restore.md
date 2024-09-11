# CV Manager Database Backup & Restore Workflows
It is expected that the database will need to be restored at some point. This document provides instructions on how to backup and restore the CV Manager database.

## Backup
1. Sign into the VM hosting the CV Manager database
1. Copy the `create-cvmanager-pgdb-backup.sh` script from the [cvmanager/scripts](../scripts/) directory to the '/home/wyocvadmin/cvmanager' directory
1. Run the script with the following command:
    ```bash
    ./create-cvmanager-pgdb-backup.sh
    ```
1. When prompted, enter the password for the database user
1. Verify that the backup was created by checking the `/home/wyocvadmin/cvmanager/pgdb-backups` directory

## Restore
1. Sign into the VM hosting the CV Manager database
1. Navigate to the `/home/wyocvadmin/cvmanager` directory
1. Make sure the sample data SQL script located at `/home/wyocvadmin/cvmanager/jpo-cvmanager/resources/sql_scripts/CVManager_SampleData.sql` has been removed
1. Copy the `restore-cvmanager-pgdb-from-backup.sh` script from the [cvmanager/scripts](../scripts/) directory to the '/home/wyocvadmin/cvmanager' directory
1. Identify the backup file to restore from (backups are located in the `/home/wyocvadmin/cvmanager/pgdb-backups` directory by default)
1. Navigate to the `/home/wyocvadmin/cvmanager` directory and run the script with the following command:
    ```bash
    ./restore-cvmanager-pgdb-from-backup.sh ./pgdb-backups/file.dump
    ```
1. When prompted, enter the password for the database user
1. Verify that no errors occurred in the logs by running `docker compose -f ./jpo-cvmanager/docker-compose.yml logs cvmanager_postgres`
1. Verify that the database has been restored by checking the database in pgAdmin4

### Note
The `pg_restore` utility will run into a number of errors during restoration due to Keycloak tables not existing. These errors can be ignored as they do not appear to affect the ability for users to log into the application post-restoration  and the tables appear to be created nevertheless.
