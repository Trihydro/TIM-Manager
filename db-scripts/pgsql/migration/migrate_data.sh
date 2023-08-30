# !/bin/bash

# if WORKING_DIR is not set, use current directory
if [ -z "$WORKING_DIR" ]
then
    WORKING_DIR=$(pwd)
    echo "WORKING_DIR is not set. Using default working directory: $WORKING_DIR"
fi

# if dependent scripts do not exist, exit
if [ ! -f "$WORKING_DIR/run_ora2pg.sh" ]
then
    echo "File $WORKING_DIR/run_ora2pg.sh does not exist. Exiting..."
    exit 1
fi
if [ ! -f "$WORKING_DIR/print_progress.sh" ]
then
    echo "File $WORKING_DIR/print_progress.sh does not exist. Exiting..."
    exit 1
fi

CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color
timestamp=$(date +%Y%m%d%H%M%S)
targetFilename="insert_data-$timestamp.sql"
truncateTablesFilepath="$WORKING_DIR/sql/truncate_tables.sql"
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
    database="intproddb"
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

generateSQL() {
    echo ""
    echo -e $YELLOW"=== Generating SQL to insert data into the new database ==="$NC
    echo ""

    echo "Clearing data directory..."
    rm -rf $WORKING_DIR/data/*

    for filename in $WORKING_DIR/config/*.conf; do
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

    echo "Enter password for user $username:"
    read -s password

    # ask user if they want to truncate tables
    echo ""
    echo "Do you want to truncate tables? (y/n)"
    read truncateTablesResponse
    if [ "$truncateTablesResponse" == "y" ]
    then
        echo -e $CYAN"Executing SQL file: $truncateTablesFilepath"$NC
        sleep 1
        PGPASSWORD=$password psql -h $host -p $port -d $database -U $username -f $truncateTablesFilepath
        echo -e $CYAN"Done executing SQL file: $truncateTablesFilepath"$NC
        sleep 1
    fi
    echo ""

    sqlLogfile="$WORKING_DIR/logs/execute_sql-$timestamp.log"

    # find out how many INSERT statements are in the files
    totalInserts=0
    for filename in $WORKING_DIR/data/*.sql; do
        inserts=$(grep -c "INSERT" $filename)
        totalInserts=$(($totalInserts + $inserts))
    done
    echo "Total INSERT statements to execute: $totalInserts"

    for filename in $WORKING_DIR/data/*.sql; do
        echo -e $CYAN"Executing SQL file: $filename"$NC
        sleep 1
        PGPASSWORD=$password psql -h $host -p $port -d $database -U $username -f $filename >> $sqlLogfile
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
    
    if [ -f "$WORKING_DIR/sql/$targetFilename" ]
    then
        echo "Removing existing file: $targetFilename"
        rm "$WORKING_DIR/sql/$targetFilename"
    fi
    outputFilepath="$WORKING_DIR/sql/$targetFilename"
    echo "Combining all data files into one file: $outputFilepath"
    dataOutputDirpath="$WORKING_DIR/data"
    if [ -z "$(ls -A $dataOutputDirpath)" ]; then
        echo "No data files to combine. Exiting..."
        exit 0
    fi
    echo -e $CYAN"Adding truncate_tables contents to the beginning of the file: $outputFilepath"$NC
    cat $truncateTablesFilepath > $outputFilepath
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

    sqlFilepath="$WORKING_DIR/sql/$targetFilename"
    echo "Executing SQL file: $sqlFilepath"
    echo "Host: $host"
    echo "Port: $port"
    echo "Database: $database"
    echo "Username: $username"
    echo ""
    psql -h $host -p $port -d $database -U $username -f $sqlFilepath

    echo ""
    echo -e $YELLOW"=== SQL File Execution Complete ==="$NC
    echo ""
}

run() {
    # if cannot ping host, exit
    ping -c 1 $host &> /dev/null
    if [ $? -ne 0 ]
    then
        echo "Cannot ping host: $host. Exiting..."
        exit 1
    fi

    # generate directories if they don't exist
    mkdir -p $WORKING_DIR/config
    mkdir -p $WORKING_DIR/data
    mkdir -p $WORKING_DIR/logs
    mkdir -p $WORKING_DIR/sql

    # if truncate_tables.sql does not exist, exit
    if [ ! -f "$truncateTablesFilepath" ]
    then
        echo "File $truncateTablesFilepath does not exist. Exiting..."
        exit 1
    fi

    # if config directory is empty, exit
    if [ -z "$(ls -A $WORKING_DIR/config)" ]; then
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