package com.trihydro.library.service.tables;

import java.util.*;

public class TimOracleTables extends OracleTables {
    
    private List<String> timTable;
    private List<String> dataFrameTable;
    private List<String> pathTable;
    private List<String> regionTable;
    private List<String> nodeXYTable;
    private List<String> pathNodeXYTable;
    private List<String> timTypeTable;
    private List<String> activeTimTable;
    private List<String> activeTimItisCodeTable;
    private List<String> timRsuTable;
    private List<String> dataFrameItisCodeTable;

    public List<String> getTimTable(){
        if(timTable != null)
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

    public List<String> getDataFrameTable(){
        if(dataFrameTable != null)
            return dataFrameTable;
        else {
            dataFrameTable = new ArrayList<String>();
            dataFrameTable.add("TIM_ID");
            return dataFrameTable;
        }
    }

    public List<String> getPathTable(){
        if(pathTable != null)
            return pathTable;
        else {
            pathTable = new ArrayList<String>();
            pathTable.add("SCALE");
            return pathTable;
        }
    }

    public List<String> getRegionTable(){
        if(regionTable != null)
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

    public List<String> getPathNodeXYTable(){
        if(pathNodeXYTable != null)
            return pathNodeXYTable;
        else {
            pathNodeXYTable = new ArrayList<String>();
            pathNodeXYTable.add("NODE_XY_ID");
            pathNodeXYTable.add("PATH_ID");
            return pathNodeXYTable;
        }
    }

    public List<String> getNodeXYTable(){
        if(nodeXYTable != null)
            return nodeXYTable;
        else {
            nodeXYTable = new ArrayList<String>();
            nodeXYTable.add("DELTA");
            nodeXYTable.add("NODE_LAT");
            nodeXYTable.add("NODE_LONG");
            return nodeXYTable;
        }
    }

    public List<String> getTimTypeTable(){
        if(timTypeTable != null)
            return timTypeTable;
        else {
            timTypeTable = new ArrayList<String>();
            timTypeTable.add("TYPE");
            timTypeTable.add("DESCRIPTION");
            return timTypeTable;
        }
    }

    public List<String> getActiveTimTable(){
        if(activeTimTable != null)
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
            return activeTimTable;
        }
    }

    public List<String> getActiveTimItisCodeTable(){
        if(activeTimItisCodeTable != null)
            return activeTimItisCodeTable;
        else {
            activeTimItisCodeTable = new ArrayList<String>();
            activeTimItisCodeTable.add("ACTIVE_TIM_ID");
            activeTimItisCodeTable.add("ITIS_CODE_ID");
            return activeTimItisCodeTable;
        }
    }

    public List<String> getTimRsuTable(){
        if(timRsuTable != null)
            return timRsuTable;
        else {
            timRsuTable = new ArrayList<String>();
            timRsuTable.add("TIM_ID");
            timRsuTable.add("RSU_ID");
            return timRsuTable;
        }
    }

    public List<String> getDataFrameItisCodeTable(){
        if(dataFrameItisCodeTable != null)
            return dataFrameItisCodeTable;
        else {
            dataFrameItisCodeTable = new ArrayList<String>();
            dataFrameItisCodeTable.add("ITIS_CODE_ID");
            dataFrameItisCodeTable.add("DATA_FRAME_ID");
            return dataFrameItisCodeTable;
        }
    }
    
    
}

