# !/bin/bash

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

# static variables
configDirLocation="$working_dir/config"
outputDirLocation="$working_dir/data"
if [ -z "$ORACLE_DB_HOST" ]
then
    echo "ORACLE_DB_HOST environment variable not set. Exiting..."
    exit 1
else
    oracle_db_host=$ORACLE_DB_HOST
fi

if [ -z "$ORACLE_DB_NAME" ]
then
    echo "ORACLE_DB_NAME environment variable not set. Exiting..."
    exit 1
else
    oracle_db_name=$ORACLE_DB_NAME
fi

if [ -z "$ORACLE_DB_PORT" ]
then
    oracle_db_port="1521"
else
    oracle_db_port=$ORACLE_DB_PORT
fi

oracle_db_url="dbi:Oracle:host=$oracle_db_host;sid=$oracle_db_name;port=$oracle_db_port"

if [ -z "$ORACLE_DB_USERNAME" ]
then
    echo "ORACLE_DB_USERNAME environment variable not set. Exiting..."
    exit 1
else
    oracle_db_username=$ORACLE_DB_USERNAME
fi

if [ -z "$ORACLE_DB_PASSWORD" ]
then
    echo "ORACLE_DB_PASSWORD environment variable not set. Exiting..."
    exit 1
else
    oracle_db_password=$ORACLE_DB_PASSWORD
fi

timestamp=$(date +%Y%m%d%H%M%S)
configFileName=$1

echo ""
echo "----------------------------------------"

# if cannot ping host, exit
if ! ping -c 1 $oracle_db_host &> /dev/null
then
    echo "Cannot ping host $oracle_db_host. Exiting."
    exit 1
fi


# if no config file specified, exit
if [ -z "$configFileName" ]
then
    echo "No config file specified. Exiting."
    exit 1
fi
echo "Using config file: $configFileName"

# if config file does not exist, exit
if [ ! -f "$configDirLocation/$configFileName" ]
then
    echo "Config file $configDirLocation/$configFileName does not exist"
    exit 1
fi

# replace .conf with $timestamp.sql
sqlFileName=$(echo $configFileName | sed 's/.conf/-'$timestamp'.sql/g')

# execute command in docker container
options="-c /config/$configFileName --out /data/$sqlFileName"
echo Executing the following command in docker container:
echo "  ora2pg $options"
docker run  \
    --name ora2pg-$timestamp \
    -e CONFIG_LOCATION=/config/$configFileName \
    -e OUTPUT_LOCATION=/data  \
    -e ORA_HOST=$oracle_db_url  \
    -e ORA_USER=$oracle_db_username  \
    -e ORA_PWD=$oracle_db_password  \
    -it \
    -v $configDirLocation:/config \
    -v $outputDirLocation:/data \
    georgmoser/ora2pg \
    ora2pg $options
echo "Outputted SQL can be found in $outputDirLocation/$sqlFileName"
echo "----------------------------------------"
echo ""