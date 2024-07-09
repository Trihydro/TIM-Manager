# CV Manager Database Setup Workflow
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
1. Ensure that the CSV file containing the RSU data is in the correct format and contains the desired data. Information on format & allowed values can be found in the [scripts README file](../scripts/README.md)
1. Run the `populate_rsus_table.sh` script with the following command:
    ```bash
    ./populate_rsus_table.sh ../data/wydot-rsu-data-cvmanager.csv
    ```
1. Sign in to the database and verify that the RSUs table has been populated by running the following query:
    ```sql
    SELECT * FROM rsus;
    ```

## 3. Creating Admin User
1. Sign into the CV Manager webapp using the default admin credentials:
    - Username: test@gmail.com
    - Password: tester
1. Navigate to the Admin page and click on the '+' button to create a new admin user
1. Set the desired email, first name & last name for the new admin user
1. Check the 'Super User' checkbox to grant the new user superuser privileges
1. Select the 'WYDOT' organization from the dropdown
1. Click 'Add User' to create the new admin user
1. Navigate to the Keycloak admin console (at port 8084) and sign in
1. Select the 'cvmanager' realm and navigate to the 'Users' tab
1. Click 'Add User'
1. Select 'Update Password' from the dropdown for required user actions
1. Fill in the username, email, first name & last name fields
1. Check 'Email verified'
1. Click 'Create'
1. Go to the 'Credentials' tab and click on 'Set Password'
1. Enter initial password (the user will be prompted to change it on first login)
1. Log out of the CV Manager webapp and log back in using the new admin user credentials
1. Update the password when prompted
1. Navigate to the Admin page and delete the default admin user

