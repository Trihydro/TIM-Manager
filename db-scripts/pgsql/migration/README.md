# PGSQL Migration
## Overview
This directory contains scripts & configuration files to migrate data from the old Oracle database to the new PostgreSQL database.

## migrate_data.sh
This is the primary script for the migration process. It is designed to be executed from the /db-scripts/pgsql/migration directory.

### Environment Variables
The following environment variables are used by the script:
* **WORKING_DIR** - The directory where the script will create temporary files. This directory must exist and be writable by the user executing the script.
* **HOST** - The hostname of the PostgreSQL server.
* **DATABASE** - The name of the PostgreSQL database.
* **USERNAME** - The username to use when connecting to the PostgreSQL server.

### Usage
The script handles SQL file generation & execution. SQL files are generated in the 'data' subdirectory of the working directory. The script will generate a separate SQL file for each table in the database. The SQL files are named using the following convention: <table_name-$timestamp>.sql.

Additionally, if the user does not want to execute each SQL file individually, the script allows the user to combine the SQL files into one file & execute that instead. The combined SQL file is named <insert_data-$timestamp.sql> and is generated in the 'sql' subdirectory of the working directory.

Prior to generating SQL files, the 'data' subdirectory is cleared of any existing files.

Prior to executing the SQL files, the 'truncate_data.sql' file is executed. This file truncates all tables in the database.

### Prerequisites
The script requires the following to be installed:
* Docker
* psql

### ora2pg
The script uses the ora2pg tool via the [georgmoser/ora2pg](https://hub.docker.com/r/georgmoser/ora2pg) docker image to generate the SQL files. The configuration files for ora2pg are located in the 'config' subdirectory of the working directory. The configuration files are named using the following convention: <table_name>.conf. Comments have been removed from the configuration files to reduce the size of the files.