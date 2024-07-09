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
| `firmware_version` | `y20.0.0`, `y20.1.0`, `y20.23.3`, `y20.39.4` |
| `target_firmware_version` | `y20.0.0`, `y20.1.0`, `y20.23.3`, `y20.39.4` |