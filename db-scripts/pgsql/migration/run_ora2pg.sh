# !/bin/bash

# if WORKING_DIR is not set, exit
if [ -z "$WORKING_DIR" ]
then
    echo "WORKING_DIR is not set. Exiting..."
    exit 1
fi

# static variables
configDirLocation="$WORKING_DIR/config"
outputDirLocation="$WORKING_DIR/data"
ip="10.145.9.179"
host="dbi:Oracle:host=$ip;sid=odevdbp01;port=1521"
user="CVCOMMS"
password="C0ll1s10n"
timestamp=$(date +%Y%m%d%H%M%S)
configFileName=$1

echo ""
echo "----------------------------------------"

# if cannot ping host, exit
if ! ping -c 1 $ip &> /dev/null
then
    echo "Cannot ping host $ip. Exiting."
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
    -e ORA_HOST=$host  \
    -e ORA_USER=$user  \
    -e ORA_PWD=$password  \
    -it \
    -v $configDirLocation:/config \
    -v $outputDirLocation:/data \
    georgmoser/ora2pg \
    ora2pg $options
echo "Outputted SQL can be found in $outputDirLocation/$sqlFileName"
echo "----------------------------------------"
echo ""