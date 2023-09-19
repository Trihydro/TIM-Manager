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

CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color
timestamp=$(date +%Y%m%d%H%M%S)
if [ -z "$PGSQL_DB_HOST" ]
then
    echo "PGSQL_DB_HOST environment variable not set. Exiting..."
    exit 1
else
    pgsql_db_host=$PGSQL_DB_HOST
fi

if [ -z "$PGSQL_DB_PORT" ]
then
    pgsql_db_port="5432"
else
    pgsql_db_port=$PGSQL_DB_PORT
fi

if [ -z "$PGSQL_DB_NAME" ]
then
    echo "PGSQL_DB_NAME environment variable not set. Exiting..."
    exit 1
else
    pgsql_db_name=$PGSQL_DB_NAME
fi

if [ -z "$PGSQL_DB_USERNAME" ]
then
    echo "PGSQL_DB_USERNAME environment variable not set. Exiting..."
    exit 1
else
    pgsql_db_username=$PGSQL_DB_USERNAME
fi

if [ -z "$PGSQL_DB_PASSWORD" ]
then
    echo "PGSQL_DB_PASSWORD environment variable not set. Exiting..."
    exit 1
else
    pgsql_db_password=$PGSQL_DB_PASSWORD
fi

executeSQLFiles() {
    echo ""
    echo -e $YELLOW"=== Executing SQL Files ==="$NC
    echo ""

    for filename in $working_dir/sql/*.sql; do
        echo -e $CYAN"Executing SQL file: $filename"$NC
        sleep 1
        PGPASSWORD=$pgsql_db_password psql -h $pgsql_db_host -p $pgsql_db_port -d $pgsql_db_name -U $pgsql_db_username -f $filename
        echo -e $CYAN"Done executing SQL file: $filename"$NC
        
        sleep 1
        echo ""
    done

    echo ""
    echo -e $YELLOW"=== SQL File Execution Complete ==="$NC
    echo ""
}

executeSQLFiles