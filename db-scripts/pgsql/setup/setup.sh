# !/bin/bash

# if WORKING_DIR is not set, use current directory
if [ -z "$WORKING_DIR" ]
then
    WORKING_DIR=$(pwd)
    echo "WORKING_DIR is not set. Using default working directory: $WORKING_DIR"
fi

CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color
timestamp=$(date +%Y%m%d%H%M%S)
if [ -z "$HOST" ]
then
    host="10.145.9.64"
    echo "HOST environment variable not set. Using default host: $host"
else
    host=$HOST
fi
port="5432"
if [ -z "$DATABASE" ]
then
    database="devdb"
    echo "DATABASE environment variable not set. Using default database: $database"
else
    database=$DATABASE
fi

if [ -z "$USERNAME" ]
then
    username="cvcomms"
    echo "USERNAME environment variable not set. Using default username: $username"
else
    username=$USERNAME
fi

executeSQLFiles() {
    echo ""
    echo -e $YELLOW"=== Executing SQL Files ==="$NC
    echo ""

    echo "Enter password for user $username:"
    read -s password

    for filename in $WORKING_DIR/*.sql; do
        echo -e $CYAN"Executing SQL file: $filename"$NC
        sleep 1
        PGPASSWORD=$password psql -h $host -p $port -d $database -U $username -f $filename
        echo -e $CYAN"Done executing SQL file: $filename"$NC
        
        sleep 1
        echo ""
    done

    echo ""
    echo -e $YELLOW"=== SQL File Execution Complete ==="$NC
    echo ""
}

executeSQLFiles