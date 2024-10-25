# CV Manager Scripts
## initialize.sql
This script initializes the CV Manager database by inserting the necessary starter data into the database. This script expects that the structure of the database has already been created.

## populate_rsus_table.sh
This script populates the RSUs table in the CV Manager database with data from a CSV file. For each row in the CSV file, the script validates the data and inserts it into the RSUs table. The script expects the CSV file to be in the following format:

```csv
latitude,longitude,milepost,ipv4_address,serial_number,iss_scms,primary_route,make,model,rsu_credential,snmp_credential,snmp_version,firmware_version,target_firmware_version
```

The header can be included, as the script will skip the first line if it is present. The script will print out any errors that occur during the validation and insertion of the data.


### CSV File Format
The CSV file should contain the following columns:
| Column Name | Description | Type | Example |
|-------------|-------------|------|---------|
| `latitude` | Latitude of the RSU | decimal | `41.3111` |
| `longitude` | Longitude of the RSU | decimal | `-111.9861` |
| `milepost` | Milepost of the RSU | decimal | `123.45` |
| `ipv4_address` | IPv4 address of the RSU | string | `172.0.0.1` |
| `serial_number` | Serial number of the RSU | string | `12345` |
| `iss_scms` | ISS SCMS identifier of the RSU | string | `12345` |
| `primary_route` | Primary route of the RSU | string | `I 80` |
| `make` | Make of the RSU | string | `Commsignia` | 
| `model`* | Model of the RSU | string | `ITS-RS4-M` | 
| `rsu_credential`* | Credentials to use to connect to the RSU | string | `default` |
| `snmp_credential`* | Credentials to use to connect to the RSU via SNMP | string | `default` |
| `snmp_version`* | SNMP Protocol to use to connect to the RSU | string | `4.1` |
| `firmware_version`* | Current firmware version of the RSU | string | `y20.0.0` |
| `target_firmware_version`* | Target firmware version of the RSU | string | `y20.1.0` |

\* See [Foreign References](#foreign-references) for allowed values.

#### Foreign References
Some columns in the CSV file reference values in other tables, so their values need to be translated to record ids. The following table describes the allowed values for these columns, which are translated to record ids in the database by the script.

| Column Name | Allowed Values | 
|-------------|-----------------|
| `model` | `ITS-RS4-M`, `RSU-2xUSB` |
| `rsu_credential` | `default`, `wydot-rsu` |
| `snmp_credential` | `default`, `wydot-snmp` | 
| `snmp_version` | `4.1`, `1218` |
| `firmware_version` | `y20.0.0`, `y20.1.0`, `y20.23.3`, `y20.39.2`, `y20.39.4`, `y20.41.3`, `y20.48.2` |
| `target_firmware_version` `y20.0.0`, `y20.1.0`, `y20.23.3`, `y20.39.2`, `y20.39.4`, `y20.41.3`, `y20.48.2` |

## create-cvmanager-pgdb-backup.sh
This script creates a backup of the CV Manager database using the `pg_dump` utility. The script will prompt the user for the password of the database user and create a backup file in the `pgdb-backups` directory.

Instructions for using this script can be found in the [`backup-restore.md`](../docs/backup-restore.md#backup) document.

## restore-cvmanager-pgdb-from-backup.sh
This script restores the CV Manager database from a backup file using the `pg_restore` utility. The script accepts the path to the backup file as an argument and will prompt the user for the password of the database user. The script will restore the database from the backup file and log any errors that occur during the restoration process. At this time, errors are thrown due to Keycloak tables not existing, but they do not appear to affect the ability for users to log into the application post-restoration and the tables appear to be created nevertheless.

Instructions for using this script can be found in the [`backup-restore.md`](../docs/backup-restore.md#restore) document.

## set_foreign_key_reference_ids.sh
This script retrieves the foreign key reference ids and sets them as environment variables. This script is used by the `populate_rsus_table.sh` script to translate the allowed values in the CSV file to record ids in the database.

## generate_csv_from_rsus_table.sh
This script generates a CSV file from the RSUs table in the CV Manager database. The script will output the CSV file to the `rsus_table.csv` file in the current directory. The CSV file will contain the following columns:

```csv
latitude,longitude,milepost,ipv4_address,serial_number,iss_scms,primary_route,make,model,rsu_credential,snmp_credential,snmp_version,firmware_version,target_firmware_version
```

The format of the CSV file is the same as the format expected by the `populate_rsus_table.sh` script.
