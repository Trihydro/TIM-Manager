#!/bin/bash

# This script generates a .csv file from the rsus table in the CV Manager database.
# The .csv file will contain the following columns:
#   - latitude
#   - longitude
#   - milepost
#   - ipv4_address
#   - serial_number
#   - iss_scms
#   - primary_route
#   - make
#   - model
#   - rsu_credential
#   - snmp_credential
#   - snmp_version
#   - firmware_version
#   - target_firmware_version

# Note: This script can be run on the VM where the CV Manager is deployed or on your local machine.

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
if [ -z "$DB_CVMANAGER_USER" ]; then
    echo "DB_CVMANAGER_USER is not set. Please set the DB_CVMANAGER_USER environment variable in the .env file."
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

# make sure psql is installed
if ! [ -x "$(command -v psql)" ]; then
    echo "Installing psql..."
    sudo apt-get update -y
    sudo apt-get install -y postgresql-client
    psql --version
fi

# if PASSWORD is not set, exit
if [ -z "$POSTGRES_PASSWORD" ]; then
    echo "Error: POSTGRES_PASSWORD is not set"
    exit 1
fi

# PGSQL info
db_password=$POSTGRES_PASSWORD

# stage one: save the query results to a temporary file
stage_one_temp_file=$(mktemp /tmp/stage_one.XXXXXXXXXX)
echo "Stage one temporary file: $stage_one_temp_file"

# query to get the rsus table
query="SELECT ST_AsText(geography), milepost, ipv4_address, serial_number, iss_scms_id, primary_route, model, credential_id, snmp_credential_id, snmp_version_id, firmware_version, target_firmware_version FROM rsus order by milepost asc"

# run the query
echo "Running query: $query"
PGPASSWORD=$db_password psql -h $DB_HOST -p $DB_PORT -U $DB_CVMANAGER_USER -d $DB_NAME -t -A -F"," -c "$query" > $stage_one_temp_file

# check if the query was successful
if [ $? -ne 0 ]; then
    echo "Error: Failed to run the query"
    exit 1
fi

# stage two: format the results
stage_two_temp_file=$(mktemp /tmp/stage_two.XXXXXXXXXX)
echo "Stage two temporary file: $stage_two_temp_file"

# add column headers
echo "latitude,longitude,milepost,ipv4_address,serial_number,iss_scms,primary_route,make,model,rsu_credential,snmp_credential,snmp_version,firmware_version,target_firmware_version" > $stage_two_temp_file

# format the results
stage_one_lines=$(wc -l < $stage_one_temp_file)
count=0
while IFS=, read -r geography milepost ipv4_address serial_number iss_scms_id primary_route model rsu_credential snmp_credential snmp_version firmware_version target_firmware_version
do
    count=$((count+1))
    echo "Processing row $count of $stage_one_lines: $geography, $milepost, $ipv4_address, $serial_number, $iss_scms_id, $primary_route, $model, $rsu_credential, $snmp_credential, $snmp_version, $firmware_version, $target_firmware_version"

    # remove POINT( and ) from the geography column, and split the latitude and longitude
    geography=$(echo $geography | sed 's/POINT(//g' | sed 's/)//g')
    latitude=$(echo $geography | cut -d' ' -f2)
    longitude=$(echo $geography | cut -d' ' -f1)

    # get foreign reference keys (make, model, rsu_credential, snmp_credential, snmp_version, firmware_version, target_firmware_version)
    make=$(PGPASSWORD=$db_password psql -h $DB_HOST -p $DB_PORT -U $DB_CVMANAGER_USER -d $DB_NAME -t -A -F"," -c "SELECT name from manufacturers WHERE manufacturer_id = (SELECT manufacturer FROM rsu_models WHERE rsu_model_id = $model)")
    model=$(PGPASSWORD=$db_password psql -h $DB_HOST -p $DB_PORT -U $DB_CVMANAGER_USER -d $DB_NAME -t -A -F"," -c "SELECT name FROM rsu_models WHERE rsu_model_id = $model")
    rsu_credential=$(PGPASSWORD=$db_password psql -h $DB_HOST -p $DB_PORT -U $DB_CVMANAGER_USER -d $DB_NAME -t -A -F"," -c "SELECT nickname FROM rsu_credentials WHERE credential_id = $rsu_credential")
    snmp_credential=$(PGPASSWORD=$db_password psql -h $DB_HOST -p $DB_PORT -U $DB_CVMANAGER_USER -d $DB_NAME -t -A -F"," -c "SELECT nickname FROM snmp_credentials WHERE snmp_credential_id = $snmp_credential")
    snmp_version=$(PGPASSWORD=$db_password psql -h $DB_HOST -p $DB_PORT -U $DB_CVMANAGER_USER -d $DB_NAME -t -A -F"," -c "SELECT nickname FROM snmp_versions WHERE snmp_version_id = $snmp_version")
    firmware_version=$(PGPASSWORD=$db_password psql -h $DB_HOST -p $DB_PORT -U $DB_CVMANAGER_USER -d $DB_NAME -t -A -F"," -c "SELECT name FROM firmware_images WHERE firmware_id = $firmware_version")
    target_firmware_version=$(PGPASSWORD=$db_password psql -h $DB_HOST -p $DB_PORT -U $DB_CVMANAGER_USER -d $DB_NAME -t -A -F"," -c "SELECT name FROM firmware_images WHERE firmware_id = $target_firmware_version")

    # if snmp_version is 'NTCIP 1218', change to NTCIP1218
    if [ "$snmp_version" == "NTCIP 1218" ]; then
        snmp_version="1218"
    fi

    # if snmp_version is 'RSU 4.1', change to 4.1
    if [ "$snmp_version" == "RSU 4.1" ]; then
        snmp_version="4.1"
    fi

    # append the formatted row to the temporary file
    echo "$latitude,$longitude,$milepost,$ipv4_address,$serial_number,$iss_scms_id,$primary_route,$make,$model,$rsu_credential,$snmp_credential,$snmp_version,$firmware_version,$target_firmware_version" >> $stage_two_temp_file
done < $stage_one_temp_file

# create a .csv file
date=$(date +"%Y-%m-%d")
csv_file="rsus_table_$date.csv"
echo "Creating $csv_file"
mv $stage_two_temp_file $csv_file

echo "Done"