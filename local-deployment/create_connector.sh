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

config_connector_class="io.debezium.connector.postgresql.PostgresConnector"
config_database_hostname="postgres"
config_database_port="5432"
config_database_user="postgres"
config_database_password="password"
config_database_dbname="postgres"
config_topic_prefix=$name
config_table_include_list="$schema.$table"
config_plugin_name="pgoutput"
config="{\"connector.class\":\"$config_connector_class\",\"database.hostname\":\"$config_database_hostname\",\"database.port\":\"$config_database_port\",\"database.user\":\"$config_database_user\",\"database.password\":\"$config_database_password\",\"database.dbname\":\"$config_database_dbname\",\"topic.prefix\":\"$config_topic_prefix\",\"table.include.list\":\"$config_table_include_list\",\"plugin.name\":\"$config_plugin_name\"}"
data="{\"name\":\"$name\",\"config\":$config}"

curl -X POST $headers $endpoint -d $data