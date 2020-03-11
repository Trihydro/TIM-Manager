package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.google.gson.Gson;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.tables.TimOracleTables;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.OdeRequestMsgMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Geometry;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Path;

@Component
public class TimService extends BaseService {

    public Gson gson = new Gson();
    private ActiveTimService activeTimService;
    private TimOracleTables timOracleTables;
    private SQLNullHandler sqlNullHandler;
    private PathService pathService;
    private RegionService regionService;
    private DataFrameService dataFrameService;
    private RsuService rsuService;
    private TimTypeService timTypeService;
    private ItisCodeService itisCodeService;
    private TimRsuService timRsuService;
    private DataFrameItisCodeService dataFrameItisCodeService;
    private PathNodeXYService pathNodeXYService;
    private NodeXYService nodeXYService;
    private Utility utility;

    @Autowired
    public void InjectDependencies(ActiveTimService _ats, TimOracleTables _timOracleTables,
            SQLNullHandler _sqlNullHandler, PathService _pathService, RegionService _regionService,
            DataFrameService _dataFrameService, RsuService _rsuService, TimTypeService _tts,
            ItisCodeService _itisCodesService, TimRsuService _timRsuService,
            DataFrameItisCodeService _dataFrameItisCodeService, PathNodeXYService _pathNodeXYService,
            NodeXYService _nodeXYService, Utility _utility) {
        activeTimService = _ats;
        timOracleTables = _timOracleTables;
        sqlNullHandler = _sqlNullHandler;
        pathService = _pathService;
        regionService = _regionService;
        dataFrameService = _dataFrameService;
        rsuService = _rsuService;
        timTypeService = _tts;
        itisCodeService = _itisCodesService;
        timRsuService = _timRsuService;
        dataFrameItisCodeService = _dataFrameItisCodeService;
        pathNodeXYService = _pathNodeXYService;
        nodeXYService = _nodeXYService;
        utility = _utility;
    }

    public void addTimToOracleDB(OdeData odeData) {

        try {

            System.out.println("Logging: " + ((OdeLogMetadata) odeData.getMetadata()).getLogFileName());

            Long timId = AddTim(odeData.getMetadata(),
                    ((OdeLogMetadata) odeData.getMetadata()).getReceivedMessageDetails(),
                    ((OdeTimPayload) odeData.getPayload()).getTim(),
                    ((OdeLogMetadata) odeData.getMetadata()).getRecordType(),
                    ((OdeLogMetadata) odeData.getMetadata()).getLogFileName(),
                    ((OdeLogMetadata) odeData.getMetadata()).getSecurityResultCode(), null, null);

            // return if TIM is not inserted
            if (timId == null)
                return;
            Long dataFrameId = null;
            Path path = null;
            Geometry geometry = null;
            OdeTravelerInformationMessage.DataFrame.Region region = null;
            DataFrame[] dFrames = ((OdeTimPayload) odeData.getPayload()).getTim().getDataframes();
            if (dFrames.length > 0) {
                us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region[] regions = dFrames[0]
                        .getRegions();
                if (regions.length > 0) {
                    region = regions[0];
                    path = regions[0].getPath();
                    geometry = regions[0].getGeometry();
                }
                dataFrameId = dataFrameService.AddDataFrame(dFrames[0], timId);
            }

            if (path != null) {
                Long pathId = pathService.InsertPath();
                regionService.AddRegion(dataFrameId, pathId, region);

                Long nodeXYId;
                for (OdeTravelerInformationMessage.NodeXY nodeXY : path.getNodes()) {
                    nodeXYId = nodeXYService.AddNodeXY(nodeXY);
                    pathNodeXYService.insertPathNodeXY(nodeXYId, pathId);
                }
            } else if (geometry != null) {
                regionService.AddRegion(dataFrameId, null, region);
            } else {
                utility.logWithDate(
                        "addTimToOracleDB - Unable to insert region, no path or geometry found (data_frame_id: "
                                + dataFrameId + ")");
            }

            if (dFrames.length > 0) {
                OdeTravelerInformationMessage.DataFrame.Region[] regions = dFrames[0].getRegions();
                if (regions.length > 0) {
                    String regionName = regions[0].getName();
                    ActiveTim activeTim = setActiveTimByRegionName(regionName);

                    // if this is an RSU TIM
                    if (activeTim != null && activeTim.getRsuTarget() != null) {
                        // save TIM RSU in DB
                        WydotRsu rsu = rsuService.getRsus().stream()
                                .filter(x -> x.getRsuTarget().equals(activeTim.getRsuTarget())).findFirst()
                                .orElse(null);
                        if (rsu != null)
                            timRsuService.AddTimRsu(timId, rsu.getRsuId(), rsu.getRsuIndex());
                    }
                }

                // save DataFrame ITIS codes
                for (String timItisCodeId : dFrames[0].getItems()) {
                    if (StringUtils.isNumeric(timItisCodeId)) {
                        String itisCodeId = getItisCodeId(timItisCodeId);
                        if (itisCodeId != null)
                            dataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, itisCodeId);
                    } else
                        dataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, timItisCodeId);
                }
            }
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    // only does one TIM at a time ***
    public void addActiveTimToOracleDB(OdeData odeData) {

        utility.logWithDate("Called addActiveTimToOracleDB");
        // variables
        ActiveTim activeTim;

        OdeTimPayload payload = (OdeTimPayload) odeData.getPayload();
        if (payload == null)
            return;
        OdeTravelerInformationMessage tim = payload.getTim();
        if (tim == null)
            return;
        DataFrame[] dframes = tim.getDataframes();
        if (dframes == null || dframes.length == 0)
            return;
        OdeTravelerInformationMessage.DataFrame.Region[] regions = dframes[0].getRegions();
        if (regions == null || regions.length == 0)
            return;
        String name = regions[0].getName();
        if (StringUtils.isEmpty(name) || StringUtils.isBlank(name))
            return;

        // get information from the region name, first check splitname length
        activeTim = setActiveTimByRegionName(name);
        if (activeTim == null)
            return;

        String satRecordId = activeTim.getSatRecordId();

        // save TIM
        Long timId = AddTim((OdeRequestMsgMetadata) odeData.getMetadata(), null, tim, null, null, null, satRecordId,
                name);

        if (timId != null) {
            // we inserted a new TIM, add additional data
            Long dataFrameId = dataFrameService.AddDataFrame(dframes[0], timId);
            addRegion(dframes[0], dataFrameId);
            addDataFrameItis(dframes[0], dataFrameId);
        } else {
            // TIM failed to insert, assume it exists and we need to fetch it
            java.sql.Timestamp ts = null;
            if (StringUtils.isNotEmpty(tim.getTimeStamp()) && StringUtils.isNotBlank(tim.getTimeStamp())) {
                ts = java.sql.Timestamp
                        .valueOf(LocalDateTime.parse(tim.getTimeStamp(), DateTimeFormatter.ISO_DATE_TIME));
            }

            timId = getTimId(tim.getPacketID(), ts);
            if (timId != null) {
                utility.logWithDate("TIM already exists, tim_id " + timId);

                // ensure we handle a new satRecordId
                if (satRecordId != null && satRecordId != "") {
                    updateTimSatRecordId(timId, satRecordId);
                    utility.logWithDate("Added sat_record_id of " + satRecordId + " to TIM with tim_id " + timId);
                }
            } else {
                // failed to insert new tim and failed to fetch existing, log and return
                utility.logWithDate(
                        "Failed to insert tim, and failed to fetch existing tim. No data inserted for OdeData: "
                                + gson.toJson(odeData));
                return;
            }
        }

        OdeRequestMsgMetadata metaData = (OdeRequestMsgMetadata) odeData.getMetadata();

        // TODO : Change to loop through RSU array - doing one rsu for now
        RSU firstRsu = null;
        if (metaData.getRequest() != null && metaData.getRequest().getRsus() != null
                && metaData.getRequest().getRsus().length > 0) {
            firstRsu = metaData.getRequest().getRsus()[0];
            activeTim.setRsuTarget(firstRsu.getRsuTarget());
        }

        if (metaData.getRequest() != null && metaData.getRequest().getSdw() != null)
            activeTim.setSatRecordId(metaData.getRequest().getSdw().getRecordId());

        activeTim.setStartDateTime(dframes[0].getStartDateTime());
        activeTim.setTimId(timId);

        // if this is an RSU TIM
        if (activeTim.getRsuTarget() != null && firstRsu != null) {
            // save TIM RSU in DB
            WydotRsu rsu = rsuService.getRsus().stream().filter(x -> x.getRsuTarget().equals(activeTim.getRsuTarget()))
                    .findFirst().orElse(null);
            timRsuService.AddTimRsu(timId, rsu.getRsuId(), firstRsu.getRsuIndex());
        }

        // set end time if duration is not indefinite
        if (dframes[0].getDurationTime() != 32000) {
            ZonedDateTime zdt = ZonedDateTime.parse(dframes[0].getStartDateTime());
            zdt = zdt.plus(dframes[0].getDurationTime(), ChronoUnit.MINUTES);
            activeTim.setEndDateTime(zdt.toString());
        }

        // if true, TIM came from WYDOT
        if (activeTim.getTimType() != null) {

            ActiveTim activeTimDb = null;

            // if RSU TIM
            if (activeTim.getRsuTarget() != null) // look for active RSU tim that matches incoming TIM
                activeTimDb = activeTimService.getActiveRsuTim(activeTim.getClientId(), activeTim.getDirection(),
                        activeTim.getRsuTarget());
            else // else look for active SAT tim that matches incoming TIM
                activeTimDb = activeTimService.getActiveSatTim(activeTim.getSatRecordId(), activeTim.getDirection());

            // if there is no active TIM, insert new one
            if (activeTimDb == null) {
                activeTimService.insertActiveTim(activeTim);
            } else { // else update active TIM
                activeTim.setActiveTimId(activeTimDb.getActiveTimId());
                activeTimService.updateActiveTim(activeTim);
            }

        } else {
            // not from WYDOT application
            // just log for now
            System.out.println("Inserting new active_tim, no TimType found - not from WYDOT application");
            activeTimService.insertActiveTim(activeTim);
        }
    }

    public Long getTimId(String packetId, Timestamp timeStamp) {
        ResultSet rs = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        Long id = null;

        try {
            connection = GetConnectionPool();
            preparedStatement = connection
                    .prepareStatement("select tim_id from tim where packet_id = ? and time_stamp = ?");
            preparedStatement.setString(1, packetId);
            preparedStatement.setTimestamp(2, timeStamp);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                id = rs.getLong("tim_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();

                if (connection != null)
                    connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return id;
    }

    public Long AddTim(OdeMsgMetadata odeTimMetadata, ReceivedMessageDetails receivedMessageDetails,
            OdeTravelerInformationMessage j2735TravelerInformationMessage, RecordType recordType, String logFileName,
            SecurityResultCode securityResultCode, String satRecordId, String regionName) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {

            String insertQueryStatement = timOracleTables.buildInsertQueryStatement("tim",
                    timOracleTables.getTimTable());
            connection = GetConnectionPool();
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "tim_id" });
            int fieldNum = 1;

            for (String col : timOracleTables.getTimTable()) {
                // default to null
                preparedStatement.setString(fieldNum, null);
                if (j2735TravelerInformationMessage != null) {
                    if (col.equals("MSG_CNT"))
                        sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                                j2735TravelerInformationMessage.getMsgCnt());
                    else if (col.equals("PACKET_ID"))
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                j2735TravelerInformationMessage.getPacketID());
                    else if (col.equals("URL_B"))
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                j2735TravelerInformationMessage.getUrlB());
                    else if (col.equals("TIME_STAMP")) {
                        String timeStamp = j2735TravelerInformationMessage.getTimeStamp();
                        java.sql.Timestamp ts = null;
                        if (StringUtils.isNotEmpty(timeStamp) && StringUtils.isNotBlank(timeStamp)) {
                            ts = java.sql.Timestamp
                                    .valueOf(LocalDateTime.parse(timeStamp, DateTimeFormatter.ISO_DATE_TIME));
                        }
                        sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, ts);
                    }
                }
                if (odeTimMetadata != null) {
                    if (col.equals("RECORD_GENERATED_BY")) {
                        if (odeTimMetadata.getRecordGeneratedBy() != null)
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                    odeTimMetadata.getRecordGeneratedBy().toString());
                        else
                            preparedStatement.setString(fieldNum, null);
                    } else if (col.equals("RECORD_GENERATED_AT")) {
                        if (odeTimMetadata.getRecordGeneratedAt() != null) {
                            java.util.Date recordGeneratedAtDate = convertDate(odeTimMetadata.getRecordGeneratedAt());
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                    mstFormat.format(recordGeneratedAtDate));
                        } else {
                            preparedStatement.setString(fieldNum, null);
                        }
                    } else if (col.equals("SCHEMA_VERSION")) {
                        sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, odeTimMetadata.getSchemaVersion());
                    } else if (col.equals("SANITIZED")) {
                        if (odeTimMetadata.isSanitized())
                            preparedStatement.setString(fieldNum, "1");
                        else
                            preparedStatement.setString(fieldNum, "0");
                    } else if (col.equals("PAYLOAD_TYPE")) {
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getPayloadType());
                    } else if (col.equals("ODE_RECEIVED_AT")) {
                        if (odeTimMetadata.getOdeReceivedAt() != null) {
                            java.util.Date receivedAtDate = convertDate(odeTimMetadata.getOdeReceivedAt());
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                    mstFormat.format(receivedAtDate));
                        } else {
                            preparedStatement.setString(fieldNum, null);
                        }
                    }

                    if (odeTimMetadata.getSerialId() != null) {
                        if (col.equals("SERIAL_ID_STREAM_ID"))
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                    odeTimMetadata.getSerialId().getStreamId());
                        else if (col.equals("SERIAL_ID_BUNDLE_SIZE"))
                            sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                                    odeTimMetadata.getSerialId().getBundleSize());
                        else if (col.equals("SERIAL_ID_BUNDLE_ID"))
                            sqlNullHandler.setLongOrNull(preparedStatement, fieldNum,
                                    odeTimMetadata.getSerialId().getBundleId());
                        else if (col.equals("SERIAL_ID_RECORD_ID"))
                            sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                                    odeTimMetadata.getSerialId().getRecordId());
                        else if (col.equals("SERIAL_ID_SERIAL_NUMBER"))
                            sqlNullHandler.setLongOrNull(preparedStatement, fieldNum,
                                    odeTimMetadata.getSerialId().getSerialNumber());
                    }
                }
                if (receivedMessageDetails != null) {
                    if (receivedMessageDetails.getLocationData() != null) {
                        if (col.equals("RMD_LD_ELEVATION")) {
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                    receivedMessageDetails.getLocationData().getElevation());
                        } else if (col.equals("RMD_LD_HEADING")) {
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                    receivedMessageDetails.getLocationData().getHeading());
                        } else if (col.equals("RMD_LD_LATITUDE")) {
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                    receivedMessageDetails.getLocationData().getLatitude());
                        } else if (col.equals("RMD_LD_LONGITUDE")) {
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                    receivedMessageDetails.getLocationData().getLongitude());
                        } else if (col.equals("RMD_LD_SPEED")) {
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                    receivedMessageDetails.getLocationData().getSpeed());
                        }
                    }
                    if (col.equals("RMD_RX_SOURCE") && receivedMessageDetails.getRxSource() != null) {
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                receivedMessageDetails.getRxSource().toString());
                    } else if (col.equals("SECURITY_RESULT_CODE")) {
                        SecurityResultCodeType securityResultCodeType = GetSecurityResultCodeTypes().stream()
                                .filter(x -> x.getSecurityResultCodeType().equals(securityResultCode.toString()))
                                .findFirst().orElse(null);
                        preparedStatement.setInt(fieldNum, securityResultCodeType.getSecurityResultCodeTypeId());
                    }
                }

                if (col.equals("SAT_RECORD_ID"))
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, satRecordId);
                else if (col.equals("TIM_NAME"))
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, regionName);
                else if (col.equals("LOG_FILE_NAME")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, logFileName);
                } else if (col.equals("RECORD_TYPE") && recordType != null) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, recordType.toString());
                }
                fieldNum++;
            }
            // execute insert statement
            Long timId = executeAndLog(preparedStatement, "timID");
            return timId;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return new Long(0);
    }

    public void addRegion(DataFrame dataFrame, Long dataFrameId) {
        Path path = null;
        Geometry geometry = null;
        Region region = dataFrame.getRegions()[0];
        path = region.getPath();
        geometry = region.getGeometry();

        if (path != null) {
            Long pathId = pathService.InsertPath();
            regionService.AddRegion(dataFrameId, pathId, region);

            Long nodeXYId;
            for (OdeTravelerInformationMessage.NodeXY nodeXY : path.getNodes()) {
                nodeXYId = nodeXYService.AddNodeXY(nodeXY);
                pathNodeXYService.insertPathNodeXY(nodeXYId, pathId);
            }
        } else if (geometry != null) {
            regionService.AddRegion(dataFrameId, null, region);
        } else {
            utility.logWithDate(
                    "addActiveTimToOracleDB - Unable to insert region, no path or geometry found (data_frame_id: "
                            + dataFrameId + ")");
        }
    }

    public void addDataFrameItis(DataFrame dataFrame, Long dataFrameId) {
        // save DataFrame ITIS codes
        String[] items = dataFrame.getItems();
        if (items == null || items.length == 0) {
            System.out.println("No itis codes found to associate with data_frame " + dataFrameId);
            return;
        }
        for (String timItisCodeId : items) {
            if (StringUtils.isNumeric(timItisCodeId)) {
                String itisCodeId = getItisCodeId(timItisCodeId);
                if (itisCodeId != null)
                    dataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, itisCodeId);
                else
                    utility.logWithDate("Could not find corresponding itis code it for " + timItisCodeId);
            } else
                dataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, timItisCodeId);
        }
    }

    public boolean updateTimSatRecordId(Long timId, String satRecordId) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {
            connection = GetConnectionPool();
            preparedStatement = connection.prepareStatement("update tim set sat_record_id = ? where tim_id = ?");
            preparedStatement.setString(1, satRecordId);
            preparedStatement.setLong(2, timId);
            return updateOrDelete(preparedStatement);
        } catch (Exception ex) {
            return false;
        } finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();

                if (connection != null)
                    connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ActiveTim setActiveTimByRegionName(String regionName) {

        if (StringUtils.isBlank(regionName) || StringUtils.isEmpty(regionName)) {
            return null;
        }

        ActiveTim activeTim = new ActiveTim();

        String[] splitName = regionName.split("_");

        if (splitName.length == 0)
            return null;

        activeTim.setDirection(splitName[0]);

        if (splitName.length > 1)
            activeTim.setRoute(splitName[1]);
        else
            return activeTim;
        if (splitName.length > 2)
            activeTim.setMilepostStart(Double.parseDouble(splitName[2]));
        else
            return activeTim;
        if (splitName.length > 3)
            activeTim.setMilepostStop(Double.parseDouble(splitName[3]));
        else
            return activeTim;
        if (splitName.length > 4) {
            // if this is an RSU TIM
            String[] hyphen_array = splitName[4].split("-");
            if (hyphen_array.length > 1) {
                if (hyphen_array[0].equals("SAT")) {
                    activeTim.setSatRecordId(hyphen_array[1]);
                } else {
                    activeTim.setRsuTarget(hyphen_array[1]);
                }
            }
        } else
            return activeTim;
        if (splitName.length > 5) {
            TimType timType = getTimType((splitName[5]));
            activeTim.setTimType(timType.getType());
            activeTim.setTimTypeId(timType.getTimTypeId());
        } else
            return activeTim;

        if (splitName.length > 6)
            activeTim.setClientId(splitName[6]);
        else
            return activeTim;

        if (splitName.length > 7) {
            try {
                Integer pk = Integer.valueOf(splitName[7]);
                activeTim.setPk(pk);
            } catch (NumberFormatException ex) {
                // the pk won't get set here
            }
        } else
            return activeTim;

        return activeTim;
    }

    public TimType getTimType(String timTypeName) {

        TimType timType = timTypeService.getTimTypes().stream().filter(x -> x.getType().equals(timTypeName)).findFirst()
                .orElse(null);

        return timType;
    }

    public String getItisCodeId(String item) {

        String itisCodeId = null;

        ItisCode itisCode = itisCodeService.selectAllItisCodes().stream()
                .filter(x -> x.getItisCode().equals(Integer.parseInt(item))).findFirst().orElse(null);
        if (itisCode != null)
            itisCodeId = itisCode.getItisCodeId().toString();

        return itisCodeId;
    }
}