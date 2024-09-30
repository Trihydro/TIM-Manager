#!/bin/bash

headers="-H Accept:application/json -H Content-Type:application/json"
endpoint="localhost:8083/connectors/"

name=$1
schema=$2
table=$3

if [ -z "$name" ]; then
  echo "name is required; usage: ./create_connector.sh <name> <schema> <table>"
  exit 1
fi

if [ -z "$schema" ]; then
  echo "schema is required; usage: ./create_connector.sh <name> <schema> <table>"
  exit 1
fi

if [ -z "$table" ]; then
  echo "table is required; usage: ./create_connector.sh <name> <schema> <table>"
  exit 1
fi

# if .env file exists, load it
working_dir=$(dirname $0)
if [ -f "$working_dir/.env" ]
then
    # if carriage returns are present, remove them
    sed -i 's/\r//g' $working_dir/.env

    echo "Loading environment variables from $working_dir/.env"
    # ignore RSU_ROUTES env variable
    eval $(grep -v '^RSU_ROUTES' $working_dir/.env | sed 's/#.*//g' | xargs)
fi

# verify required environment variables
if [ -z "$POSTGRES_DB_HOSTNAME" ]; then
  echo "POSTGRES_DB_HOSTNAME is required"
  exit 1
fi

if [ -z "$POSTGRES_USER" ]; then
  echo "POSTGRES_USER is required"
  exit 1
fi

if [ -z "$POSTGRES_PASSWORD" ]; then
  echo "POSTGRES_PASSWORD is required"
  exit 1
fi

if [ -z "$POSTGRES_DB_NAME" ]; then
  echo "POSTGRES_DB_NAME is required"
  exit 1
fi

config_connector_class="io.debezium.connector.postgresql.PostgresConnector"
config_database_hostname=$POSTGRES_DB_HOSTNAME
config_database_port="5432"
config_database_user=$POSTGRES_USER
config_database_password=$POSTGRES_PASSWORD
config_database_dbname=$POSTGRES_DB_NAME
config_topic_prefix=$name
config_table_include_list="$schema.$table"
config_plugin_name="pgoutput"
config="{\"connector.class\":\"$config_connector_class\",\"database.hostname\":\"$config_database_hostname\",\"database.port\":\"$config_database_port\",\"database.user\":\"$config_database_user\",\"database.password\":\"$config_database_password\",\"database.dbname\":\"$config_database_dbname\",\"topic.prefix\":\"$config_topic_prefix\",\"table.include.list\":\"$config_table_include_list\",\"plugin.name\":\"$config_plugin_name\"}"
data="{\"name\":\"$name\",\"config\":$config}"

curl -X POST $headers $endpoint -d $data
if [ $? -ne 0 ]; then
  echo "Failed to create connector"
  exit 1
fi
exit 0