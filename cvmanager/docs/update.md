# CV Manager Database Update Workflows
Over time the data in the CV Manager database may need to be updated. This document provides instructions on how to update the database for various scenarios.

## Adding RSUs
### Using Current Values
If the RSUs do not use any new models, credentials, SNMP versions, or firmware versions, the process for adding RSUs is as follows:
1. Add the new RSUs to the `wydot-rsu-data-cvmanager.csv` file in the `cvmanager/data` directory.
1. Navigate to the `cvmanager/scripts` directory.
1. Run the `populate_rsus_table.sh` script with the following command:
    ```bash
    ./populate_rsus_table.sh ../data/wydot-rsu-data-cvmanager.csv
    ```
1. Sign in to the database and verify that the RSUs table has been populated by running the following query:
    ```sql
    SELECT * FROM rsus;
    ```

### Using New Values
If the RSUs use new models, credentials, SNMP versions, or firmware versions, the process for adding RSUs is as follows:
1. Add the new RSUs to the `wydot-rsu-data-cvmanager.csv` file in the `cvmanager/data` directory.
1. Add the new models, credentials, SNMP versions, and firmware versions to the `initialize.sql` script in the `cvmanager/scripts` directory.
1. Add the new models, credentials, SNMP versions, and firmware versions to the appropriate tables in the database.
1. Add support for the new models, credentials, SNMP versions, and firmware versions to the `populate_rsus_table.sh` script in the `cvmanager/scripts` directory.
1. Navigate to the `cvmanager/scripts` directory.
1. Run the `populate_rsus_table.sh` script with the following command:
    ```bash
    ./populate_rsus_table.sh ../data/wydot-rsu-data-cvmanager.csv
    ```
1. Sign in to the database and verify that the RSUs table has been populated by running the following query:
    ```sql
    SELECT * FROM rsus;
    ```

For more instructions on adding new models, credentials, SNMP versions, and firmware versions, see the appropriate sections linked below.
- [Adding Models](#adding-models)
- [Adding RSU Credentials](#adding-rsu-credentials)
- [Adding SNMP Credentials](#adding-snmp-credentials)
- [Adding Firmware Versions](#adding-firmware-versions)
- [Adding SNMP Versions](#adding-snmp-versions)

## Adding Users
1. Sign into the CV Manager webapp.
1. Navigate to the 'Admin' page and click on the 'Users' tab.
1. Click on the '+' button to add a new user.
1. Fill out the form with the new user's information.
1. Check the 'Super User' box if the new user should have super user privileges.
1. Select the 'WYDOT' organization from the 'Organizations' dropdown.
1. Click 'Add User' to save the new user.
1. Navigate to the Keycloak admin console (at port 8084) and sign in
1. Select the 'cvmanager' realm and navigate to the 'Users' tab
1. Click 'Add User'
1. Select 'Update Password' from the dropdown for required user actions
1. Fill in the username, email, first name & last name fields
1. Check 'Email verified'
1. Click 'Create'
1. Go to the 'Credentials' tab and click on 'Set Password'
1. Enter initial password (the user will be prompted to change it on first login)

## Adding Models
1. Add the new model to the `initialize.sql` script in the `cvmanager/scripts` directory.
1. Add the new model to the 'allowed values' column of the table in the [Foreign References](../scripts/README.md#foreign-references) section of the scripts README file.
1. Update the `populate_rsus_table.sh` script in the `cvmanager/scripts` directory to support the new model.
1. Open pgAdmin4 and sign in to the database.
1. Add the new model to the `rsu_models` table in the database by running the following query:
    ```sql
    INSERT INTO public.rsu_models(
        name, supported_radio, manufacturer)
        VALUES ('<modelname>', '<supportedradiolist>', <manufacturerid>);
    ```

## Adding RSU Credentials
1. Add the new rsu_credential to the `initialize.sql` script in the `cvmanager/scripts` directory.
1. Add the new rsu_credential to the 'allowed values' column of the table in the [Foreign References](../scripts/README.md#foreign-references) section of the scripts README file.
1. Update the `populate_rsus_table.sh` script in the `cvmanager/scripts` directory to support the new rsu_credential.
1. Open pgAdmin4 and sign in to the database.
1. Add the new rsu_credential to the `rsu_credentials` table in the database by running the following query:
    ```sql
    INSERT INTO public.rsu_credentials(
        username, password, snmp_version)
        VALUES ('<username>', '<password>', '<nickname>');
    ```

## Adding SNMP Credentials
1. Add the new snmp_credential to the `initialize.sql` script in the `cvmanager/scripts` directory.
1. Add the new snmp_credential to the 'allowed values' column of the table in the [Foreign References](../scripts/README.md#foreign-references) section of the scripts README file.
1. Update the `populate_rsus_table.sh` script in the `cvmanager/scripts` directory to support the new snmp_credential.
1. Open pgAdmin4 and sign in to the database.
1. Add the new snmp_credential to the `snmp_credentials` table in the database by running the following query:
    ```sql
    INSERT INTO public.snmp_credentials(
	    username, password, encrypt_password, nickname)
	    VALUES ('<username>', '<password>', '<encryptionpassword>', '<nickname>');
    ```

## Adding Firmware Versions
1. Add the new firmware_version to the `initialize.sql` script in the `cvmanager/scripts` directory.
1. Add the new firmware_version to the 'allowed values' column of the table in the [Foreign References](../scripts/README.md#foreign-references) section of the scripts README file.
1. Update the `populate_rsus_table.sh` script in the `cvmanager/scripts` directory to support the new firmware_version.
1. Open pgAdmin4 and sign in to the database.
1. Add the new firmware_version to the `firmware_versions` table in the database by running the following query:
    ```sql
    INSERT INTO public.firmware_images(
	    name, model, install_package, version)
	    VALUES ('<nickname>', <modelid>, '<filename>', '<fullversion>');
    ```

## Adding SNMP Versions
1. Add the new snmp_version to the `initialize.sql` script in the `cvmanager/scripts` directory.
1. Add the new snmp_version to the 'allowed values' column of the table in the [Foreign References](../scripts/README.md#foreign-references) section of the scripts README file.
1. Update the `populate_rsus_table.sh` script in the `cvmanager/scripts` directory to support the new snmp_version.
1. Open pgAdmin4 and sign in to the database.
1. Add the new snmp_version to the `snmp_versions` table in the database by running the following query:
    ```sql
    INSERT INTO public.snmp_versions(
        version_code, nickname)
        VALUES ('<versioncode>', '<nickname>');
    ```