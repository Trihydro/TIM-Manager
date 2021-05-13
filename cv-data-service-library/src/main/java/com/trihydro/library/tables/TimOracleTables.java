package com.trihydro.library.tables;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TimOracleTables extends OracleTables {

    private List<String> timTable;
    private List<String> dataFrameTable;
    private List<String> pathTable;
    private List<String> regionTable;
    private List<String> nodeXYTable;
    private List<String> nodeLLTable;
    private List<String> pathNodeXYTable;
    private List<String> pathNodeLLTable;
    private List<String> timTypeTable;
    private List<String> activeTimTable;
    private List<String> activeTimHoldingTable;
    private List<String> timRsuTable;
    private List<String> dataFrameItisCodeTable;
    private List<String> driverAlertItisCodeTable;

    public List<String> getTimTable() {
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
            timTable.add("SAT_RECORD_ID");

            return timTable;
        }
    }

    public List<String> getDataFrameTable() {
        if (dataFrameTable != null)
            return dataFrameTable;
        else {
            dataFrameTable = new ArrayList<String>();
            dataFrameTable.add("TIM_ID");
            dataFrameTable.add("SSP_TIM_RIGHTS");
            dataFrameTable.add("FRAME_TYPE");
            // dataFrameTable.add("MSG_ID"); //ignore msg_id for now since its a full object
            // dataFrameTable.add("FURTHER_INFO_ID"); //ignore further_info_id for now
            // dataFrameTable.add("VIEW_ANGLE"); //not part of ode DataFrame object
            // dataFrameTable.add("MUTCD"); //not part of ode DataFrame object
            // dataFrameTable.add("CRC"); //not part of ode DataFrame object
            dataFrameTable.add("DURATION_TIME");
            dataFrameTable.add("PRIORITY");
            dataFrameTable.add("SSP_LOCATION_RIGHTS");
            dataFrameTable.add("SSP_MSG_TYPES");
            dataFrameTable.add("SSP_MSG_CONTENT");
            dataFrameTable.add("CONTENT");
            dataFrameTable.add("URL");
            // dataFrameTable.add("POSITION_LAT"); //not part of ode DataFrame object
            // dataFrameTable.add("POSITION_LONG"); //not part of ode DataFrame object
            // dataFrameTable.add("POSITION_ELEV"); //not part of ode DataFrame object
            dataFrameTable.add("START_DATE_TIME");
            return dataFrameTable;
        }
    }

    public List<String> getPathTable() {
        if (pathTable != null)
            return pathTable;
        else {
            pathTable = new ArrayList<String>();
            pathTable.add("SCALE");
            return pathTable;
        }
    }

    public List<String> getRegionTable() {
        if (regionTable != null)
            return regionTable;
        else {
            regionTable = new ArrayList<String>();
            regionTable.add("DATA_FRAME_ID");
            regionTable.add("NAME");
            regionTable.add("LANE_WIDTH");
            regionTable.add("DIRECTIONALITY");
            regionTable.add("DIRECTION");
            regionTable.add("CLOSED_PATH");
            regionTable.add("ANCHOR_LAT");
            regionTable.add("ANCHOR_LONG");

            regionTable.add("PATH_ID");

            regionTable.add("GEOMETRY_DIRECTION");
            regionTable.add("GEOMETRY_EXTENT");
            regionTable.add("GEOMETRY_LANE_WIDTH");

            regionTable.add("GEOMETRY_CIRCLE_POSITION_LAT");
            regionTable.add("GEOMETRY_CIRCLE_POSITION_LONG");
            regionTable.add("GEOMETRY_CIRCLE_POSITION_ELEV");
            regionTable.add("GEOMETRY_CIRCLE_RADIUS");
            regionTable.add("GEOMETRY_CIRCLE_UNITS");
            return regionTable;
        }
    }

    public List<String> getPathNodeXYTable() {
        if (pathNodeXYTable != null)
            return pathNodeXYTable;
        else {
            pathNodeXYTable = new ArrayList<String>();
            pathNodeXYTable.add("NODE_XY_ID");
            pathNodeXYTable.add("PATH_ID");
            return pathNodeXYTable;
        }
    }

    public List<String> getPathNodeLLTable() {
        if (pathNodeLLTable != null)
            return pathNodeLLTable;
        else {
            pathNodeLLTable = new ArrayList<String>();
            pathNodeLLTable.add("NODE_LL_ID");
            pathNodeLLTable.add("PATH_ID");
            return pathNodeLLTable;
        }
    }

    public List<String> getNodeXYTable() {
        if (nodeXYTable != null)
            return nodeXYTable;
        else {
            nodeXYTable = new ArrayList<String>();
            nodeXYTable.add("DELTA");
            nodeXYTable.add("NODE_LAT");
            nodeXYTable.add("NODE_LONG");
            nodeXYTable.add("X");
            nodeXYTable.add("Y");
            nodeXYTable.add("ATTRIBUTES_DWIDTH");
            nodeXYTable.add("ATTRIBUTES_DELEVATION");
            return nodeXYTable;
        }
    }

    public List<String> getNodeLLTable() {
        if (nodeLLTable != null)
            return nodeLLTable;
        else {
            nodeLLTable = new ArrayList<String>();
            nodeLLTable.add("DELTA");
            nodeLLTable.add("NODE_LAT");
            nodeLLTable.add("NODE_LONG");
            nodeLLTable.add("X");
            nodeLLTable.add("Y");
            nodeLLTable.add("ATTRIBUTES_DWIDTH");
            nodeLLTable.add("ATTRIBUTES_DELEVATION");
            return nodeLLTable;
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

    public List<String> getActiveTimTable() {
        if (activeTimTable != null)
            return activeTimTable;
        else {
            activeTimTable = new ArrayList<String>();
            activeTimTable.add("TIM_ID");
            activeTimTable.add("DIRECTION");
            activeTimTable.add("TIM_START");
            activeTimTable.add("TIM_END");
            activeTimTable.add("TIM_TYPE_ID");
            activeTimTable.add("ROUTE");
            activeTimTable.add("CLIENT_ID");
            activeTimTable.add("SAT_RECORD_ID");
            activeTimTable.add("PK");
            activeTimTable.add("START_LATITUDE");
            activeTimTable.add("START_LONGITUDE");
            activeTimTable.add("END_LATITUDE");
            activeTimTable.add("END_LONGITUDE");
            activeTimTable.add("EXPIRATION_DATE");
            activeTimTable.add("PROJECT_KEY");
            return activeTimTable;
        }
    }

    public List<String> getActiveTimHoldingTable() {
        if (activeTimHoldingTable != null)
            return activeTimHoldingTable;
        else {
            activeTimHoldingTable = new ArrayList<String>();
            activeTimHoldingTable.add("ACTIVE_TIM_HOLDING_ID");
            activeTimHoldingTable.add("CLIENT_ID");
            activeTimHoldingTable.add("DIRECTION");
            activeTimHoldingTable.add("RSU_TARGET");
            activeTimHoldingTable.add("SAT_RECORD_ID");
            activeTimHoldingTable.add("START_LATITUDE");
            activeTimHoldingTable.add("START_LONGITUDE");
            activeTimHoldingTable.add("END_LATITUDE");
            activeTimHoldingTable.add("END_LONGITUDE");
            activeTimHoldingTable.add("RSU_INDEX");
            activeTimHoldingTable.add("DATE_CREATED");
            activeTimHoldingTable.add("PROJECT_KEY");
            activeTimHoldingTable.add("EXPIRATION_DATE");
            activeTimHoldingTable.add("PACKET_ID");
            return activeTimHoldingTable;
        }
    }

    public List<String> getTimRsuTable() {
        if (timRsuTable != null)
            return timRsuTable;
        else {
            timRsuTable = new ArrayList<String>();
            timRsuTable.add("TIM_ID");
            timRsuTable.add("RSU_ID");
            timRsuTable.add("RSU_INDEX");
            return timRsuTable;
        }
    }

    public List<String> getDataFrameItisCodeTable() {
        if (dataFrameItisCodeTable != null)
            return dataFrameItisCodeTable;
        else {
            dataFrameItisCodeTable = new ArrayList<String>();
            dataFrameItisCodeTable.add("ITIS_CODE_ID");
            dataFrameItisCodeTable.add("DATA_FRAME_ID");
            dataFrameItisCodeTable.add("TEXT");
            dataFrameItisCodeTable.add("POSITION");
            return dataFrameItisCodeTable;
        }
    }

    public List<String> getDriverAlertItisCodeTable() {
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
