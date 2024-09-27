#!/bin/bash

# This script is used to create a connector to monitor the county_roads_v1_h table in the countyrds schema

table_name="county_roads_v1_h"
schema_name="countyrds"
connector_name="adhoc_conditions_monitor"

./create_connector.sh $connector_name $schema_name $table_name
if [ $? -ne 0 ]; then
  echo "Failed to create connector"
  exit 1
fi