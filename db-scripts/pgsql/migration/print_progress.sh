usage="Usage: $0 <sqlExecuteLogFilename> <totalInserts>"

sqlLogfile=$1
if [ -z "$sqlLogfile" ]
then
    echo $usage
    exit 1
fi

totalInserts=$2
if [ -z "$totalInserts" ]
then
    echo $usage
    exit 1
fi

totalRecordsInserted=$(grep -c "INSERT" $sqlLogfile)
progressInDecimal=$(echo "scale=5; $totalRecordsInserted / $totalInserts" | bc | sed 's/^\./0./')
progressAsPercentage=$(echo "scale=5; $progressInDecimal * 100" | bc | sed 's/^\./0./') # 0.12345 -> 12.345
progressAsPercentageWithTwoDecimalPlaces=$(printf "%.2f" $progressAsPercentage) # 12.345 -> 12.34
echo "Total records inserted: $totalRecordsInserted/$totalInserts. Progress: $progressAsPercentageWithTwoDecimalPlaces%"
sleep 1