# CV Manager Database Setup
The CV Manager database is a PostgreSQL database. This document provides instructions on how to set up the database for the first time.

## 1. Initializing the Database
1. Navigate to the root directory of the jpo-cvmanager project
1. Remove sample data SQL script using `rm resources/sql_scripts/CVManager_SampleData.sql`
1. Spin up the database using `docker compose up -d cvmanager_postgres`
1. Open pgAdmin4 and connect to the database
1. Open [initialize.sql](../resources/sql_scripts/initial_data.sql) and run the script to initialize the database

## 2. Populating the RSUs Table
1. Navigate to the [cvmanager/scripts](../scripts) directory
1. Set POSTGRES_PASSWORD environment variable to the password of the database
1. Run the `populate_rsus_table.sh` script with the following command:
    ```bash
    ./populate_rsus_table.sh ../data/
    ``
1. Sign in to the database and verify that the RSUs table has been populated

## 3. Creating Admin User
(TBD)

## 4. Cleaning Up Sample Data
(TBD)