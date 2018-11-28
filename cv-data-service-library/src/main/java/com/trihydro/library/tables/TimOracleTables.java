package com.trihydro.library.tables;

import java.util.*;

public class TimOracleTables extends OracleTables {

    private static List<String> timTable;
    private static List<String> dataFrameTable;
    private static List<String> pathTable;
    private static List<String> regionTable;
    private static List<String> nodeXYTable;
    private static List<String> pathNodeXYTable;
    private static List<String> rsuIndexTable;
    private List<String> timTypeTable;
    private static List<String> activeTimTable;
    private static List<String> timRsuTable;
    private static List<String> dataFrameItisCodeTable;
    private static List<String> driverAlertItisCodeTable;

    public static List<String> getTimTable() {
        if (timTable != null)
            return timTable;
        else {
            timTable = new ArrayList<String>();

            timTable.add("MSG_CNT");
            timTable.add("PACKET_ID");
            timTable.add("URL_B");
            timTable.add("TIME_STAMP");
            timTable.add("RECORD_GENERATED_BY");
            timTable.add("RMD_LD_ELEVATION");
            timTable.add("RMD_LD_HEADING");
            timTable.add("RMD_LD_LATITUDE");
            timTable.add("RMD_LD_LONGITUDE");
            timTable.add("RMD_LD_SPEED");
            timTable.add("RMD_RX_SOURCE");
            timTable.add("SCHEMA_VERSION");
            timTable.add("SECURITY_RESULT_CODE");
            timTable.add("LOG_FILE_NAME");
            timTable.add("RECORD_GENERATED_AT");
            timTable.add("SANITIZED");
            timTable.add("SERIAL_ID_STREAM_ID");
            timTable.add("SERIAL_ID_BUNDLE_SIZE");
            timTable.add("SERIAL_ID_BUNDLE_ID");
            timTable.add("SERIAL_ID_RECORD_ID");
            timTable.add("SERIAL_ID_SERIAL_NUMBER");
            timTable.add("PAYLOAD_TYPE");
            timTable.add("RECORD_TYPE");
            timTable.add("ODE_RECEIVED_AT");
            timTable.add("RSU_INDEX");

            return timTable;
        }
    }

    public static List<String> getDataFrameTable() {
        if (dataFrameTable != null)
            return dataFrameTable;
        else {
            dataFrameTable = new ArrayList<String>();
            dataFrameTable.add("TIM_ID");
            return dataFrameTable;
        }
    }

    public static List<String> getPathTable() {
        if (pathTable != null)
            return pathTable;
        else {
            pathTable = new ArrayList<String>();
            pathTable.add("SCALE");
            return pathTable;
        }
    }

    public static List<String> getRegionTable() {
        if (regionTable != null)
            return regionTable;
        else {
            regionTable = new ArrayList<String>();
            regionTable.add("DATA_FRAME_ID");
            regionTable.add("PATH_ID");
            regionTable.add("ANCHOR_LAT");
            regionTable.add("ANCHOR_LONG");
            return regionTable;
        }
    }

    public static List<String> getPathNodeXYTable() {
        if (pathNodeXYTable != null)
            return pathNodeXYTable;
        else {
            pathNodeXYTable = new ArrayList<String>();
            pathNodeXYTable.add("NODE_XY_ID");
            pathNodeXYTable.add("PATH_ID");
            return pathNodeXYTable;
        }
    }

    public static List<String> getRsuIndexTable() {
        if (rsuIndexTable != null)
            return rsuIndexTable;
        else {
            rsuIndexTable = new ArrayList<String>();
            rsuIndexTable.add("RSU_ID");
            rsuIndexTable.add("RSU_INDEX");
            return rsuIndexTable;
        }
    }

    public static List<String> getNodeXYTable() {
        if (nodeXYTable != null)
            return nodeXYTable;
        else {
            nodeXYTable = new ArrayList<String>();
            nodeXYTable.add("DELTA");
            nodeXYTable.add("NODE_LAT");
            nodeXYTable.add("NODE_LONG");
            return nodeXYTable;
        }
    }

    public List<String> getTimTypeTable() {
        if (timTypeTable != null)
            return timTypeTable;
        else {
            timTypeTable = new ArrayList<String>();
            timTypeTable.add("TYPE");
            timTypeTable.add("DESCRIPTION");
            return timTypeTable;
        }
    }

    public static List<String> getActiveTimTable() {
        if (activeTimTable != null)
            return activeTimTable;
        else {
            activeTimTable = new ArrayList<String>();
            activeTimTable.add("TIM_ID");
            activeTimTable.add("MILEPOST_START");
            activeTimTable.add("MILEPOST_STOP");
            activeTimTable.add("DIRECTION");
            activeTimTable.add("TIM_START");
            activeTimTable.add("TIM_END");
            activeTimTable.add("TIM_TYPE_ID");
            activeTimTable.add("ROUTE");
            activeTimTable.add("CLIENT_ID");
            activeTimTable.add("SAT_RECORD_ID");
            activeTimTable.add("PK");
            return activeTimTable;
        }
    }

    public static List<String> getTimRsuTable() {
        if (timRsuTable != null)
            return timRsuTable;
        else {
            timRsuTable = new ArrayList<String>();
            timRsuTable.add("TIM_ID");
            timRsuTable.add("RSU_ID");
            return timRsuTable;
        }
    }

    public static List<String> getDataFrameItisCodeTable() {
        if (dataFrameItisCodeTable != null)
            return dataFrameItisCodeTable;
        else {
            dataFrameItisCodeTable = new ArrayList<String>();
            dataFrameItisCodeTable.add("ITIS_CODE_ID");
            dataFrameItisCodeTable.add("DATA_FRAME_ID");
            dataFrameItisCodeTable.add("TEXT");
            return dataFrameItisCodeTable;
        }
    }

    public static List<String> getDriverAlertItisCodeTable() {
        if (driverAlertItisCodeTable != null)
            return driverAlertItisCodeTable;
        else {
            driverAlertItisCodeTable = new ArrayList<String>();
            driverAlertItisCodeTable.add("ITIS_CODE_ID");
            driverAlertItisCodeTable.add("DRIVER_ALERT_ID");
            return driverAlertItisCodeTable;
        }
    }

}
