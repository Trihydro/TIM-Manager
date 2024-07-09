# CV Manager Database Backup & Restore Workflows
It is expected that the database will need to be restored at some point. This document provides instructions on how to backup and restore the CV Manager database.

## Backup
1. Sign into the VM hosting the CV Manager database
1. Copy the `create-cvmanager-pgdb-backup.sh` script from the `cvmanager/scripts` directory to the '/home/wyocvadmin/cvmanager' directory
1. Run the script with the following command:
    ```bash
    ./create-cvmanager-pgdb-backup.sh
    ```
1. Verify that the backup was created by checking the `/home/wyocvadmin/cvmanager/pgdb-backups` directory

## Restore
(TBD)