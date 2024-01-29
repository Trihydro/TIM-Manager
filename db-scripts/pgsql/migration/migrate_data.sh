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

# if dependent scripts do not exist, exit
if [ ! -f "$working_dir/run_ora2pg.sh" ]
then
    echo "File $working_dir/run_ora2pg.sh does not exist. Exiting..."
    exit 1
fi
if [ ! -f "$working_dir/print_progress.sh" ]
then
    echo "File $working_dir/print_progress.sh does not exist. Exiting..."
    exit 1
fi

CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color
timestamp=$(date +%Y%m%d%H%M%S)
targetFilename="insert_data-$timestamp.sql"
truncateTablesFilepath="$working_dir/sql/truncate_tables.sql"
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

generateSQL() {
    echo ""
    echo -e $YELLOW"=== Generating SQL to insert data into the new database ==="$NC
    echo ""

    echo "Clearing data directory..."
    rm -rf $working_dir/data/*

    for filename in $working_dir/config/*.conf; do
        baseFilename=$(basename $filename)
        echo -e $CYAN"Exporting data with config: $baseFilename"$NC
        ./run_ora2pg.sh $baseFilename
        echo ""
    done

    echo ""
    echo -e $YELLOW"=== SQL File Generation Complete ==="$NC
    echo ""
}

executeSQLFiles() {
    echo ""
    echo -e $YELLOW"=== Executing SQL Files ==="$NC
    echo ""

    # ask user if they want to truncate tables
    echo ""
    echo "Do you want to truncate tables? (y/n)"
    read truncateTablesResponse
    if [ "$truncateTablesResponse" == "y" ]
    then
        generateTruncateScriptBasedOnConfigFileNames
        echo -e $CYAN"Executing SQL file: $truncateTablesFilepath"$NC
        sleep 1
        PGPASSWORD=$pgsql_db_password psql -h $pgsql_db_host -p $pgsql_db_port -d $pgsql_db_name -U $pgsql_db_username -f $truncateTablesFilepath
        echo -e $CYAN"Done executing SQL file: $truncateTablesFilepath"$NC
        sleep 1
    fi
    echo ""

    sqlLogfile="$working_dir/logs/execute_sql-$timestamp.log"

    # find out how many INSERT statements are in the files
    totalInserts=0
    for filename in $working_dir/data/*.sql; do
        inserts=$(grep -c "INSERT" $filename)
        totalInserts=$(($totalInserts + $inserts))
    done
    echo "Total INSERT statements to execute: $totalInserts"

    for filename in $working_dir/data/*.sql; do
        echo -e $CYAN"Executing SQL file: $filename"$NC
        sleep 1
        PGPASSWORD=$pgsql_db_password psql -h $pgsql_db_host -p $pgsql_db_port -d $pgsql_db_name -U $pgsql_db_username -f $filename >> $sqlLogfile
        echo -e $CYAN"Done executing SQL file: $filename"$NC

        recordsInserted=$(grep -c "INSERT" $filename)
        echo "Records inserted: $recordsInserted"

        ./print_progress.sh $sqlLogfile $totalInserts
        
        sleep 1
        echo ""
    done

    echo ""
    echo -e $YELLOW"=== SQL File Execution Complete ==="$NC
    echo ""
}

combineSQL() {
    echo ""
    echo -e $YELLOW"=== Combining SQL Files ==="$NC
    echo ""
    
    if [ -f "$working_dir/sql/$targetFilename" ]
    then
        echo "Removing existing file: $targetFilename"
        rm "$working_dir/sql/$targetFilename"
    fi
    outputFilepath="$working_dir/sql/$targetFilename"
    echo "Combining all data files into one file: $outputFilepath"
    dataOutputDirpath="$working_dir/data"
    if [ -z "$(ls -A $dataOutputDirpath)" ]; then
        echo "No data files to combine. Exiting..."
        exit 0
    fi

    # ask user if they want to add truncate statements
    echo ""
    echo "Do you want to add truncate statements to the beginning of the file? (y/n)"
    read truncateTablesResponse
    if [ "$truncateTablesResponse" == "y" ]
    then
        generateTruncateScriptBasedOnConfigFileNames
        echo -e $CYAN"Adding truncate_tables contents to the beginning of the file: $outputFilepath"$NC
        cat $truncateTablesFilepath > $outputFilepath
        sleep 1
    fi
    echo ""

    for filename in $dataOutputDirpath/*; do
        echo -e $CYAN"Appending file contents: $filename"$NC
        cat $filename >> $outputFilepath
    done

    echo ""
    echo -e $YELLOW"=== SQL File Combination Complete ==="$NC
    echo ""
}

executeCombinedSQL() {
    echo ""
    echo -e $YELLOW"=== Executing Combined SQL File ==="$NC
    echo ""

    sqlFilepath="$working_dir/sql/$targetFilename"
    echo "Executing SQL file: $sqlFilepath"
    echo "Host: $pgsql_db_host"
    echo "Port: $pgsql_db_port"
    echo "Database: $pgsql_db_name"
    echo "Username: $pgsql_db_username"
    echo ""
    PGPASSWORD=$pgsql_db_password psql -h $pgsql_db_host -p $pgsql_db_port -d $pgsql_db_name -U $pgsql_db_username -f $sqlFilepath

    echo ""
    echo -e $YELLOW"=== SQL File Execution Complete ==="$NC
    echo ""
}

generateTruncateScriptBasedOnConfigFileNames() {
    truncateTablesFilepath="$working_dir/sql/truncate_tables.sql"
    echo "Generating truncate tables script: $truncateTablesFilepath"
    echo "" > $truncateTablesFilepath
    for filename in $working_dir/config/*.conf; do
        baseFilename=$(basename $filename)
        # lose .conf and the ##- prefix
        # example : 01-test.conf -> test
        tableName=$(echo $baseFilename | sed 's/\.conf//g' | sed 's/^[0-9][0-9]-//g')
        echo "TRUNCATE TABLE $tableName CASCADE;" >> $truncateTablesFilepath
    done
    # delete first line
    sed -i '1d' $truncateTablesFilepath
    echo "Done generating truncate tables script: $truncateTablesFilepath"
}

run() {
    # if cannot ping host, exit
    ping -c 1 $pgsql_db_host &> /dev/null
    if [ $? -ne 0 ]
    then
        echo "Cannot ping host: $pgsql_db_host. Exiting..."
        exit 1
    fi

    # generate directories if they don't exist
    mkdir -p $working_dir/config
    mkdir -p $working_dir/data
    mkdir -p $working_dir/logs
    mkdir -p $working_dir/sql

    # if config directory is empty, exit
    if [ -z "$(ls -A $working_dir/config)" ]; then
        echo "Config directory is empty. Exiting..."
        exit 1
    fi

    # ask user if they want to generate the sql file
    echo ""
    echo "Do you want to generate the SQL files? (y/n)"
    read generateSQLResponse
    if [ "$generateSQLResponse" == "y" ]
    then
        generateSQL
    fi

    # ask user if they want to execute the sql files
    echo ""
    echo "Do you want to execute the SQL files one at a time? (y/n)"
    read executeSQLResponse
    if [ "$executeSQLResponse" == "y" ]
    then
        executeSQLFiles
    else
        # ask user if they want to combine the sql files into one file
        echo ""
        echo "Do you want to combine the SQL files into one file? (y/n)"
        read executeCombinedSQLResponse
        if [ "$executeCombinedSQLResponse" == "y" ]
        then
            combineSQL

            # ask user if they want to execute the combined sql file
            echo ""
            echo "Do you want to execute the combined SQL file? (y/n)"
            read executeCombinedSQLResponse
            if [ "$executeCombinedSQLResponse" == "y" ]
            then
                executeCombinedSQL
            fi
        fi
    fi
}

run