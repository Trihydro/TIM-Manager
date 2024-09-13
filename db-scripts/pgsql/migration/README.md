# PGSQL Migration
## Overview
This directory contains scripts & configuration files to migrate data from the old Oracle database to the new PostgreSQL database.

## migrate_data.sh
This is the primary script for the migration process. It is designed to be executed from the /db-scripts/pgsql/migration directory.

### Environment Variables
The following environment variables are used by the script:
* **PGSQL_DB_HOST** - The hostname of the PostgreSQL server.
* **PGSQL_DB_PORT** - The port number of the PostgreSQL server.
* **PGSQL_DB_NAME** - The name of the PostgreSQL database.
* **PGSQL_DB_USERNAME** - The username to use when connecting to the PostgreSQL database.
* **PGSQL_DB_PASSWORD** - The password to use when connecting to the PostgreSQL database.

See [sample.env](#sample.env) for an example of how to set these environment variables.

### Usage
The script handles SQL file generation (via the `run_ora2pg.sh` script) & execution (via `psql`). SQL files are generated in the 'data' subdirectory of the working directory. The script will generate a separate SQL file for each table in the database. The SQL files are named using the following convention: <##-table_name-$timestamp>.sql.

Additionally, if the user does not want to execute each SQL file individually, the script allows the user to combine the SQL files into one file & execute that instead. The combined SQL file is named <insert_data-$timestamp.sql> and is generated in the 'sql' subdirectory of the working directory.

Prior to generating SQL files, the 'data' subdirectory is cleared of any existing files.

Prior to executing the SQL files, the user is asked if they want to truncate the data first. If the user chooses to truncate the data, the `truncate_data.sql` file is executed. This file is located in the 'sql' subdirectory of the working directory.

### Prerequisites
The script requires the following to be installed:
* Docker
* psql

## run_ora2pg.sh
This script is used to generate the SQL files using the ora2pg tool. It is designed to be executed from the /db-scripts/pgsql/migration directory.

### Environment Variables
The following environment variables are used by the script:
* **ORACLE_DB_HOST** - The hostname of the Oracle server.
* **ORACLE_DB_PORT** - The port number of the Oracle server.
* **ORACLE_DB_NAME** - The name of the Oracle database.
* **ORACLE_DB_USERNAME** - The username to use when connecting to the Oracle database.
* **ORACLE_DB_PASSWORD** - The password to use when connecting to the Oracle database.
* **PG_VERSION** - The version of PostgreSQL to use when generating the SQL files.

See [sample.env](#sample.env) for an example of how to set these environment variables.

### Usage
The script handles SQL file generation. SQL files are generated in the 'data' subdirectory of the working directory. The script will generate a separate SQL file for each table in the database. The SQL files are named using the following convention: <table_name-$timestamp>.sql.

Prior to generating SQL files, the 'data' subdirectory is cleared of any existing files.

The user should not have to execute this script directly. It is called by the `migrate_data.sh` script.

### Prerequisites
The script requires the following to be installed:
* Docker

## print_progress.sh
This script is used to print the progress of the migration process when executing SQL scripts one at a time. It is designed to be executed from the /db-scripts/pgsql/migration directory by the `migrate_data.sh` script. The user should not have to execute this script directly.

## sample.env
A sample.env file is provided in this directory. Rename this file to .env and update the values to match your environment.

## ora2pg
The migration process uses the ora2pg tool via the [georgmoser/ora2pg](https://hub.docker.com/r/georgmoser/ora2pg) docker image to generate the SQL files. The configuration files for ora2pg are located in the 'config' subdirectory of the working directory. The configuration files are named using the following convention: <table_name>.conf. Comments have been removed from the configuration files to reduce the size of the files.

## Note on `mileposts` table
The `mileposts` table is excluded from the automated migration process, as its data does not change frequently. The data in this table should be manually migrated from the old Oracle database to the new PostgreSQL database using the `mileposts.sql` file located in the 'sql' subdirectory of the working directory.

## Note on triggers
A number of triggers should be disabled during the migration process and enabled after. The `disable_triggers.sql` file is provided in the 'sql' subdirectory of the working directory. This file will disable all necessary triggers in the database. The `enable_triggers.sql` file is also provided in the 'sql' subdirectory of the working directory. This file will enable all necessary triggers in the database.