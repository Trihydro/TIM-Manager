#!/bin/bash

# This script reads in values from a .csv file and populates the rsus table in the postgres database.
# Some columns of the rsus table contain foreign references to keys in other tables. This script translates
# the values in the .csv file to the corresponding ids in the other tables to maintain referential integrity.

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
if [ -z "$DB_USER" ]; then
    echo "DB_USER is not set. Please set the DB_USER environment variable in the .env file."
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

echo "Retrieving ids for foreign key references..."
source ./set_foreign_key_reference_ids.sh
if [[ $? -ne 0 ]]; then
    echo "Error: failed to retrieve ids for foreign key references"
    exit 1
fi

if $DEBUG; then
    # print foreign key reference ids
    echo "" 
    echo "Printing foreign key reference ids..."
    echo "----------------------------------------"
    echo "commsignia_manufacturer_id: $commsignia_manufacturer_id"
    echo "itsRs4M_model_id: $itsRs4M_model_id"
    echo "rsu2xUsb_model_id: $rsu2xUsb_model_id"
    echo "default_rsu_credential_id: $default_rsu_credential_id"
    echo "wydot_rsu_credential_id: $wydot_rsu_credential_id"
    echo "default_snmp_credential_id: $default_snmp_credential_id"
    echo "wydot_snmp_credential_id: $wydot_snmp_credential_id"
    echo "fourDot1_snmp_version_id: $fourDot1_snmp_version_id"
    echo "twelve18_snmp_version_id: $twelve18_snmp_version_id"
    echo "y20_0_0_firmware_version_id: $y20_0_0_firmware_version_id"
    echo "y20_1_0_firmware_version_id: $y20_1_0_firmware_version_id"
    echo "y20_23_3_firmware_version_id: $y20_23_3_firmware_version_id"
    echo "y20_39_2_firmware_version_id: $y20_39_2_firmware_version_id"
    echo "y20_39_4_firmware_version_id: $y20_39_4_firmware_version_id"
    echo "y20_41_3_firmware_version_id: $y20_41_3_firmware_version_id"
    echo "y20_48_2_firmware_version_id: $y20_48_2_firmware_version_id"
    echo "wydot_organization_id: $wydot_organization_id"
    echo "----------------------------------------"
    echo ""
fi

# read in values from .csv using awk
echo "Reading in values from .csv file..."
filename=$1
if [ -z "$filename" ]; then
    echo "Error: no .csv file specified"
    exit 1
fi
while IFS=, read -r latitude longitude milepost ipv4_address serial_number iss_scms primary_route make model rsu_credential snmp_credential snmp_version firmware_version target_firmware_version; do
    latitude=$latitude
    longitude=$longitude
    milepost=$milepost
    ipv4_address=$ipv4_address
    serial_number=$serial_number
    iss_scms=$iss_scms
    primary_route=$primary_route
    make=$make
    model=$model
    rsu_credential=$rsu_credential
    snmp_credential=$snmp_credential
    snmp_version=$snmp_version
    firmware_version=$firmware_version
    target_firmware_version=$target_firmware_version

    echo ""

    # if header, skip
    if [ "$latitude" = "latitude" ]; then
        echo "Header detected, skipping..."
        continue
    fi

    # if RSU is already in rsus table, skip it
    numRecords=`PGPASSWORD=$db_password psql -d $DB_NAME -U $DB_USER -h $DB_HOST -p $DB_PORT -Atc "select COUNT(*) FROM cvmanager.rsus WHERE serial_number='$serial_number';"`
    if [ $numRecords -gt 0 ]; then
        echo "RSU '$serial_number' is already in rsus table, skipping..."
        continue
    fi

    # translate values to ids
    if [ "$DEBUG" = "true" ]; then
        echo "Translating values to ids for model, rsu_credential, snmp_credential, snmp_version, firmware_version, and target_firmware_version..."
    fi
    
    # Model
    if [ "$model" = "ITS-RS4-M" ]; then
        model_id=$itsRs4M_model_id
    elif [ "$model" = "RSU-2xUSB" ]; then
        model_id=$rsu2xUsb_model_id
    else
        echo "Error: invalid model '$model'"
        exit 1
    fi

    # RSU Credential
    if [ "$rsu_credential" = "default" ]; then
        rsu_credential_id=$default_rsu_credential_id
    elif [ "$rsu_credential" = "wydot-rsu" ]; then
        rsu_credential_id=$wydot_rsu_credential_id
    else
        echo "Error: invalid rsu_credential '$rsu_credential' for RSU '$serial_number'"
        exit 1
    fi

    # SNMP Credential
    if [ "$snmp_credential" = "default" ]; then
        snmp_credential_id=$default_snmp_credential_id
    elif [ "$snmp_credential" = "wydot-snmp" ]; then
        snmp_credential_id=$wydot_snmp_credential_id
    else
        echo "Error: invalid snmp_credential '$snmp_credential' for RSU '$serial_number'"
        exit 1
    fi

    # SNMP Version
    if [ "$snmp_version" = "4.1" ]; then
        snmp_version_id=$fourDot1_snmp_version_id
    elif [ "$snmp_version" = "1218" ]; then
        snmp_version_id=$twelve18_snmp_version_id
    else
        echo "Error: invalid snmp_version '$snmp_version' for RSU '$serial_number'"
        exit 1
    fi

    # Firmware Version
    if [ "$firmware_version" = "y20.0.0" ]; then
        firmware_version_id=$y20_0_0_firmware_version_id
    elif [ "$firmware_version" = "y20.1.0" ]; then
        firmware_version_id=$y20_1_0_firmware_version_id
    elif [ "$firmware_version" = "y20.23.3" ]; then
        firmware_version_id=$y20_23_3_firmware_version_id
    elif [ "$firmware_version" = "y20.39.2" ]; then
        firmware_version_id=$y20_39_2_firmware_version_id
    elif [ "$firmware_version" = "y20.39.4" ]; then
        firmware_version_id=$y20_39_4_firmware_version_id
    elif [ "$firmware_version" = "y20.41.3" ]; then
        firmware_version_id=$y20_41_3_firmware_version_id
    elif [ "$firmware_version" = "y20.48.2" ]; then
        firmware_version_id=$y20_48_2_firmware_version_id
    else
        echo "Error: invalid firmware_version '$firmware_version' for RSU '$serial_number'"
        exit 1
    fi

    # Target Firmware Version
    if [ "$target_firmware_version" = "y20.0.0" ]; then
        target_firmware_version_id=$y20_0_0_firmware_version_id
    elif [ "$target_firmware_version" = "y20.1.0" ]; then
        target_firmware_version_id=$y20_1_0_firmware_version_id
    elif [ "$target_firmware_version" = "y20.23.3" ]; then
        target_firmware_version_id=$y20_23_3_firmware_version_id
    elif [ "$target_firmware_version" = "y20.39.2" ]; then
        target_firmware_version_id=$y20_39_2_firmware_version_id
    elif [ "$target_firmware_version" = "y20.39.4" ]; then
        target_firmware_version_id=$y20_39_4_firmware_version_id
    elif [ "$target_firmware_version" = "y20.41.3" ]; then
        target_firmware_version_id=$y20_41_3_firmware_version_id
    elif [ "$target_firmware_version" = "y20.48.2" ]; then
        target_firmware_version_id=$y20_48_2_firmware_version_id
    else
        echo "Error: invalid target_firmware_version '$target_firmware_version' for RSU '$serial_number'"
        exit 1
    fi

    if $DEBUG; then
        # print RSU info
        echo ""
        echo "Printing RSU info..."
        echo "----------------------------------------"
        echo "latitude: $latitude"
        echo "longitude: $longitude"
        echo "milepost: $milepost"
        echo "ipv4_address: $ipv4_address"
        echo "serial_number: $serial_number"
        echo "iss_scms: $iss_scms"
        echo "primary_route: $primary_route"
        echo "model: $model_id"
        echo "rsu_credential: $rsu_credential_id"
        echo "snmp_credential: $snmp_credential_id"
        echo "snmp_version: $snmp_version_id"
        echo "firmware_version: $firmware_version_id"
        echo "target_firmware_version: $target_firmware_version_id"
        echo "----------------------------------------"
        echo ""
    fi

    # add RSU to rsus table
    echo "Adding RSU $serial_number to rsus table..."
    PGPASSWORD=$db_password psql -d $DB_NAME -U $DB_USER -h $DB_HOST -p $DB_PORT -c "INSERT INTO cvmanager.rsus(geography, milepost, ipv4_address, serial_number, iss_scms_id, primary_route, model, credential_id, snmp_credential_id, snmp_version_id, firmware_version, target_firmware_version) VALUES (ST_GeomFromText('POINT($longitude $latitude)'), $milepost, '$ipv4_address', '$serial_number', '$iss_scms', '$primary_route', $model_id, $rsu_credential_id, $snmp_credential_id, $snmp_version_id, $firmware_version_id, $target_firmware_version_id);"
    if [[ $? -ne 0 ]]; then
        echo "Error: failed to add RSU $serial_number to rsus table"
        exit 1
    fi

    # associate RSU with organization
    echo "Associating RSU $serial_number with WYDOT organization..."
    PGPASSWORD=$db_password psql -d $DB_NAME -U $DB_USER -h $DB_HOST -p $DB_PORT -c "INSERT INTO cvmanager.rsu_organization(rsu_id, organization_id) VALUES ((SELECT rsu_id FROM cvmanager.rsus WHERE serial_number='$serial_number'), $wydot_organization_id);"
    if [[ $? -ne 0 ]]; then
        echo "Error: failed to associate RSU $serial_number with WYDOT organization"
        exit 1
    fi

done < $filename