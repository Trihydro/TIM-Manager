package com.trihydro.library.tables;

import java.util.*;

public class DriverAlertOracleTables extends OracleTables {
    
    private static List<String> driverAlertTable;
    private List<String> dataFrameTable;

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
            driverAlertTable.add("RECORD_GENERATED_BY");
            driverAlertTable.add("SCHEMA_VERSION");
            driverAlertTable.add("SECURITY_RESULT_CODE");
            driverAlertTable.add("LOG_FILE_NAME");
            driverAlertTable.add("RECORD_GENERATED_AT");
            driverAlertTable.add("SANITIZED");
            driverAlertTable.add("SERIAL_ID_STREAM_ID");
            driverAlertTable.add("SERIAL_ID_BUNDLE_SIZE");
            driverAlertTable.add("SERIAL_ID_BUNDLE_ID");
            driverAlertTable.add("SERIAL_ID_RECORD_ID");
            driverAlertTable.add("SERIAL_ID_SERIAL_NUMBER");
            driverAlertTable.add("PAYLOAD_TYPE");
            driverAlertTable.add("RECORD_TYPE");
            driverAlertTable.add("ODE_RECEIVED_AT");
            return driverAlertTable;
        }
    }

    public List<String> getDataFrameTable(){
        if(dataFrameTable != null)
            return dataFrameTable;
        else {
            dataFrameTable = new ArrayList<String>();
            dataFrameTable.add("TIM_ID");
            return dataFrameTable;
        }
    }
}

