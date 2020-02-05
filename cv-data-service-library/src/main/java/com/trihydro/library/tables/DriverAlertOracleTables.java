package com.trihydro.library.tables;

import java.util.ArrayList;
import java.util.List;

public class DriverAlertOracleTables extends OracleTablesStatic {
    
    private static List<String> driverAlertTable;

    public static List<String> getDriverAlertTable(){
        if(driverAlertTable != null)
            return driverAlertTable;
        else {
            driverAlertTable = new ArrayList<String>();            
            driverAlertTable.add("LATITUDE");
            driverAlertTable.add("LONGITUDE");
            driverAlertTable.add("HEADING");
            driverAlertTable.add("ELEVATION_M");
            driverAlertTable.add("SPEED");
            driverAlertTable.add("DRIVER_ALERT_TYPE_ID");
            driverAlertTable.add("LOG_FILE_NAME");
            driverAlertTable.add("RECORD_TYPE");
            driverAlertTable.add("PAYLOAD_TYPE");
            driverAlertTable.add("SERIAL_ID_STREAM_ID");
            driverAlertTable.add("SERIAL_ID_BUNDLE_SIZE");
            driverAlertTable.add("SERIAL_ID_BUNDLE_ID");
            driverAlertTable.add("SERIAL_ID_RECORD_ID");
            driverAlertTable.add("SERIAL_ID_SERIAL_NUMBER");
            driverAlertTable.add("ODE_RECEIVED_AT");
            driverAlertTable.add("SCHEMA_VERSION");
            driverAlertTable.add("RECORD_GENERATED_AT");
            driverAlertTable.add("RECORD_GENERATED_BY");
            driverAlertTable.add("SANITIZED");
            driverAlertTable.add("SECURITY_RESULT_CODE");
            return driverAlertTable;
        }
    }
    
}

