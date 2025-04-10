package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.model.CertExpirationModel;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.RegionNameElementCollection;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.tables.TimDbTables;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class TimService extends BaseService {

    public Gson gson = new Gson();
    private ActiveTimService activeTimService;
    private TimDbTables timDbTables;
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
    private ActiveTimHoldingService activeTimHoldingService;
    private PathNodeLLService pathNodeLLService;
    private NodeLLService nodeLLService;

    @Autowired
    public void InjectDependencies(ActiveTimService _ats, TimDbTables _timDbTables,
                                   SQLNullHandler _sqlNullHandler, PathService _pathService, RegionService _regionService,
                                   DataFrameService _dataFrameService, RsuService _rsuService, TimTypeService _tts,
                                   ItisCodeService _itisCodesService, TimRsuService _timRsuService,
                                   DataFrameItisCodeService _dataFrameItisCodeService, PathNodeXYService _pathNodeXYService,
                                   NodeXYService _nodeXYService, Utility _utility, ActiveTimHoldingService _athService,
                                   PathNodeLLService _pathNodeLLService,
                                   NodeLLService _nodeLLService) { // TODO: use constructor instead of InjectDependencies
        activeTimService = _ats;
        timDbTables = _timDbTables;
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
        activeTimHoldingService = _athService;
        pathNodeLLService = _pathNodeLLService;
        nodeLLService = _nodeLLService;
    }

    public void addTimToDatabase(OdeData odeData) {

        try {
            log.info("Called addTimToDatabase");

            ReceivedMessageDetails rxMsgDet = null;
            RecordType recType = null;
            String logFileName = null;
            SecurityResultCode secResCode = null;
            if (odeData.getMetadata() instanceof OdeLogMetadata) {
                var odeLogMetadata = (OdeLogMetadata) odeData.getMetadata();
                rxMsgDet = odeLogMetadata.getReceivedMessageDetails();
                recType = odeLogMetadata.getRecordType();
                logFileName = odeLogMetadata.getLogFileName();
                secResCode = odeLogMetadata.getSecurityResultCode();
            }

            Long timId = AddTim(odeData.getMetadata(), rxMsgDet, getTim((OdeTimPayload) odeData.getPayload()),
                recType, logFileName, secResCode, null, null);

            // return if TIM is not inserted
            if (timId == null) {
                return;
            }

            DataFrame[] dFrames = getTim((OdeTimPayload) odeData.getPayload()).getDataframes();
            if (dFrames.length == 0) {
                log.info("addTimToDatabase - No dataframes found in TIM (tim_id: {})", timId);
                return;
            }
            OdeTravelerInformationMessage.DataFrame firstDataFrame = dFrames[0];
            Long dataFrameId = dataFrameService.AddDataFrame(firstDataFrame, timId);

            us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region[] regions = firstDataFrame.getRegions();
            addRegions(firstDataFrame, dataFrameId);

            String firstRegionName = regions[0].getName(); // all regions have the same name
            ActiveTim activeTim = setActiveTimByRegionName(firstRegionName);

            // if this is an RSU TIM
            if (activeTim != null && activeTim.getRsuTarget() != null) {
                // save TIM RSU in DB
                rsuService.getRsus().stream()
                    .filter(x -> x.getRsuTarget().equals(activeTim.getRsuTarget())).findFirst()
                    .ifPresent(rsu -> timRsuService.AddTimRsu(timId, rsu.getRsuId(), rsu.getRsuIndex()));
            }

            addDataFrameItis(firstDataFrame, dataFrameId);

        } catch (NullPointerException e) {
            log.info("Null pointer exception encountered in TimService.addTimToDatabase() method: {}", e.getMessage());
        }
    }

    /**
     * Adds an active TIM to the database. This only handles a single TIM at a time.
     */
    public void addActiveTimToDatabase(OdeData odeData) {
        log.info("Called addActiveTimToDatabase");

        ActiveTim activeTim;

        OdeTimPayload payload = (OdeTimPayload) odeData.getPayload();
        if (payload == null) {
            return;
        }
        OdeTravelerInformationMessage tim = getTim(payload);
        if (tim == null) {
            return;
        }
        DataFrame[] dframes = tim.getDataframes();
        if (dframes == null || dframes.length == 0) {
            return;
        }
        OdeTravelerInformationMessage.DataFrame.Region[] regions = dframes[0].getRegions();
        if (regions == null || regions.length == 0) {
            return;
        }
        String firstRegionName = regions[0].getName();
        if (StringUtils.isEmpty(firstRegionName) || StringUtils.isBlank(firstRegionName)) {
            return;
        }
        OdeRequestMsgMetadata metaData = (OdeRequestMsgMetadata) odeData.getMetadata();
        if (metaData == null) {
            return;
        }

        // get information from the region name, first check splitname length
        activeTim = setActiveTimByRegionName(firstRegionName);
        if (activeTim == null) {
            return;
        }

        String satRecordId = activeTim.getSatRecordId();

        // see if TIM exists already
        java.sql.Timestamp ts = null;
        if (StringUtils.isNotEmpty(tim.getTimeStamp()) && StringUtils.isNotBlank(tim.getTimeStamp())) {
            ts = java.sql.Timestamp.valueOf(LocalDateTime.parse(tim.getTimeStamp(), DateTimeFormatter.ISO_DATE_TIME));
        }
        Long timId = getTimId(tim.getPacketID(), ts);

        if (timId == null) {
            // TIM doesn't currently exist. Add it.
            timId = AddTim(metaData, null, tim, null, null, null, satRecordId, firstRegionName);

            if (timId != null) {
                // we inserted a new TIM, add additional data
                Long dataFrameId = dataFrameService.AddDataFrame(dframes[0], timId);
                addRegions(dframes[0], dataFrameId);
                addDataFrameItis(dframes[0], dataFrameId);
            } else {
                // failed to insert new tim and failed to fetch existing, log and return
                log.info("Failed to insert tim, and failed to fetch existing tim. No data inserted for OdeData: {}", gson.toJson(odeData));
                return;
            }
        } else {
            log.info("TIM already exists, tim_id {}", timId);
        }

        // ensure we handle a new satRecordId
        if (satRecordId != null && !satRecordId.isEmpty()) {
            updateTimSatRecordId(timId, satRecordId);
            log.info("Added sat_record_id of {} to TIM with tim_id {}", satRecordId, timId);
        }

        // TODO : Change to loop through RSU array - doing one rsu for now
        RSU firstRsu = null;
        if (metaData.getRequest() != null && metaData.getRequest().getRsus() != null
            && metaData.getRequest().getRsus().length > 0) {
            firstRsu = metaData.getRequest().getRsus()[0];
            activeTim.setRsuTarget(firstRsu.getRsuTarget());
        }

        if (metaData.getRequest() != null && metaData.getRequest().getSdw() != null) {
            activeTim.setSatRecordId(metaData.getRequest().getSdw().getRecordId());
        }

        // the ODE now parses all dataframes to find the most recent and sets it
        // to this new OdeTimStartDateTime. We'll take advantage.
        // Occasionally the OdeTimStartDateTime is null...set to dfames[0] startDateTime
        // in that case
        var stDate = metaData.getOdeTimStartDateTime();
        if (StringUtils.isEmpty(stDate)) {
            stDate = dframes[0].getStartDateTime();
            log.info("addActiveTimToDatabase did not find odeTimStartDateTime, setting to dataframe value {}", stDate);
        }
        activeTim.setStartDateTime(stDate);
        activeTim.setTimId(timId);

        ActiveTimHolding ath = null;

        // if this is an RSU TIM
        if (activeTim.getRsuTarget() != null && firstRsu != null) {
            // save TIM RSU in DB
            WydotRsu rsu = rsuService.getRsus().stream().filter(x -> x.getRsuTarget().equals(activeTim.getRsuTarget()))
                .findFirst().orElse(null);
            if (rsu != null) {
                timRsuService.AddTimRsu(timId, rsu.getRsuId(), rsu.getRsuIndex());
            }
            ath = activeTimHoldingService.getRsuActiveTimHolding(activeTim.getClientId(), activeTim.getDirection(),
                activeTim.getRsuTarget());

            if (ath == null) {
                log.info("Could not find active_tim_holding for client_id '{}', direction '{}', rsu_target '{}'",
                    activeTim.getClientId(), activeTim.getDirection(), activeTim.getRsuTarget());
            }
        } else {
            // SDX tim, fetch holding
            ath = activeTimHoldingService.getSdxActiveTimHolding(activeTim.getClientId(), activeTim.getDirection(),
                activeTim.getSatRecordId());

            if (ath == null) {
                log.info("Could not find active_tim_holding for client_id '{}', direction '{}', sat_record_id '{}'",
                    activeTim.getClientId(), activeTim.getDirection(), activeTim.getSatRecordId());
            }
        }

        // set end time if duration is not indefinite
        if (dframes[0].getDurationTime() != 32000) {
            ZonedDateTime zdt = ZonedDateTime.parse(dframes[0].getStartDateTime());
            zdt = zdt.plusMinutes(dframes[0].getDurationTime());
            activeTim.setEndDateTime(zdt.toString());
        }

        if (ath != null) {
            // set activeTim start/end points from holding table
            activeTim.setStartPoint(ath.getStartPoint());
            activeTim.setEndPoint(ath.getEndPoint());

            // set projectKey
            activeTim.setProjectKey(ath.getProjectKey());

            // set expiration time if found
            if (StringUtils.isNotBlank(ath.getExpirationDateTime())) {
                activeTim.setExpirationDateTime(ath.getExpirationDateTime());
            }
        }

        // if true, TIM came from WYDOT
        if (activeTim.getTimType() != null) {

            ActiveTim activeTimDb = null;

            // if RSU TIM
            if (activeTim.getRsuTarget() != null) // look for active RSU tim that matches incoming TIM
            {
                activeTimDb = activeTimService.getActiveRsuTim(activeTim.getClientId(), activeTim.getDirection(),
                    activeTim.getRsuTarget());
            } else // else look for active SAT tim that matches incoming TIM
            {
                activeTimDb = activeTimService.getActiveSatTim(activeTim.getSatRecordId(), activeTim.getDirection());
            }

            // if there is no active TIM, insert new one
            if (activeTimDb == null) {
                activeTimService.insertActiveTim(activeTim);
            } else { // else update active TIM
                // If we couldn't find an Active TIM Holding record, we should persist the
                // existing values
                // for startPoint, endPoint, and projectKey
                if (ath == null) {
                    activeTim.setStartPoint(activeTimDb.getStartPoint());
                    activeTim.setEndPoint(activeTimDb.getEndPoint());
                    activeTim.setProjectKey(activeTimDb.getProjectKey());
                }
                activeTim.setActiveTimId(activeTimDb.getActiveTimId());
                activeTimService.updateActiveTim(activeTim);
            }

        } else {
            // not from WYDOT application
            // just log for now
            log.info("Inserting new active_tim, no TimType found - not from WYDOT application");
            activeTimService.insertActiveTim(activeTim);
        }

        // remove active_tim_holding now that we've saved its values
        if (ath != null) {
            activeTimHoldingService.deleteActiveTimHolding(ath.getActiveTimHoldingId());
        }
    }

    public Long getTimId(String packetId, Timestamp timeStamp) {
        Long id = null;

        try (
            Connection connection = dbInteractions.getConnectionPool();
            PreparedStatement preparedStatement = connection
                .prepareStatement("select tim_id from tim where packet_id = ? and time_stamp = ?");
        ) {
            preparedStatement.setString(1, packetId);
            preparedStatement.setTimestamp(2, timeStamp);

            try (
                ResultSet rs = preparedStatement.executeQuery();
            ) {
                if (rs.next()) {
                    id = rs.getLong("tim_id");
                }
            }

        } catch (Exception e) {
            log.error("Failed to get tim_id from database", e);
        }
        return id;
    }

    public Long AddTim(OdeMsgMetadata odeTimMetadata, ReceivedMessageDetails receivedMessageDetails,
                       OdeTravelerInformationMessage j2735TravelerInformationMessage, RecordType recordType, String logFileName,
                       SecurityResultCode securityResultCode, String satRecordId, String regionName) {
        String insertQueryStatement = timDbTables.buildInsertQueryStatement("tim",
            timDbTables.getTimTable());

        try (
            Connection connection = dbInteractions.getConnectionPool();
            PreparedStatement preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"tim_id"});
        ) {
            int fieldNum = 1;

            for (String col : timDbTables.getTimTable()) {
                // default to null
                preparedStatement.setString(fieldNum, null);
                if (j2735TravelerInformationMessage != null) {
                    if (col.equals("MSG_CNT")) {
                        sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                            j2735TravelerInformationMessage.getMsgCnt());
                    } else if (col.equals("PACKET_ID")) {
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                            j2735TravelerInformationMessage.getPacketID());
                    } else if (col.equals("URL_B")) {
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                            j2735TravelerInformationMessage.getUrlB());
                    } else if (col.equals("TIME_STAMP")) {
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
                        if (odeTimMetadata.getRecordGeneratedBy() != null) {
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                odeTimMetadata.getRecordGeneratedBy().toString());
                        } else {
                            preparedStatement.setString(fieldNum, null);
                        }
                    } else if (col.equals("RECORD_GENERATED_AT")) {
                        if (odeTimMetadata.getRecordGeneratedAt() != null) {
                            java.util.Date recordGeneratedAtDate = utility.convertDate(odeTimMetadata.getRecordGeneratedAt());
                            Timestamp ts = new Timestamp(recordGeneratedAtDate.getTime());
                            sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, ts);
                        } else {
                            preparedStatement.setString(fieldNum, null);
                        }
                    } else if (col.equals("SCHEMA_VERSION")) {
                        sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, odeTimMetadata.getSchemaVersion());
                    } else if (col.equals("SANITIZED")) {
                        if (odeTimMetadata.isSanitized()) {
                            preparedStatement.setInt(fieldNum, 1);
                        } else {
                            preparedStatement.setInt(fieldNum, 0);
                        }
                    } else if (col.equals("PAYLOAD_TYPE")) {
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getPayloadType());
                    } else if (col.equals("ODE_RECEIVED_AT")) {
                        if (odeTimMetadata.getOdeReceivedAt() != null) {
                            java.util.Date receivedAtDate = utility.convertDate(odeTimMetadata.getOdeReceivedAt());
                            Timestamp ts = new Timestamp(receivedAtDate.getTime());
                            sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, ts);
                        } else {
                            preparedStatement.setTimestamp(fieldNum, null);
                        }
                    }

                    if (odeTimMetadata.getSerialId() != null) {
                        if (col.equals("SERIAL_ID_STREAM_ID")) {
                            sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                                odeTimMetadata.getSerialId().getStreamId());
                        } else if (col.equals("SERIAL_ID_BUNDLE_SIZE")) {
                            sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                                odeTimMetadata.getSerialId().getBundleSize());
                        } else if (col.equals("SERIAL_ID_BUNDLE_ID")) {
                            sqlNullHandler.setLongOrNull(preparedStatement, fieldNum,
                                odeTimMetadata.getSerialId().getBundleId());
                        } else if (col.equals("SERIAL_ID_RECORD_ID")) {
                            sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                                odeTimMetadata.getSerialId().getRecordId());
                        } else if (col.equals("SERIAL_ID_SERIAL_NUMBER")) {
                            sqlNullHandler.setLongOrNull(preparedStatement, fieldNum,
                                odeTimMetadata.getSerialId().getSerialNumber());
                        }
                    }
                }
                if (receivedMessageDetails != null) {
                    if (receivedMessageDetails.getLocationData() != null) {
                        if (col.equals("RMD_LD_ELEVATION")) {
                            sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                Double.parseDouble(receivedMessageDetails.getLocationData().getElevation()));
                        } else if (col.equals("RMD_LD_HEADING")) {
                            sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                Double.parseDouble(receivedMessageDetails.getLocationData().getHeading()));
                        } else if (col.equals("RMD_LD_LATITUDE")) {
                            sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                Double.parseDouble(receivedMessageDetails.getLocationData().getLatitude()));
                        } else if (col.equals("RMD_LD_LONGITUDE")) {
                            sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                Double.parseDouble(receivedMessageDetails.getLocationData().getLongitude()));
                        } else if (col.equals("RMD_LD_SPEED")) {
                            sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                Double.parseDouble(receivedMessageDetails.getLocationData().getSpeed()));
                        }
                    } else {
                        // location data is null, set all to null (with correct type)
                        if (col.equals("RMD_LD_ELEVATION") || col.equals("RMD_LD_HEADING") || col.equals("RMD_LD_LATITUDE")
                            || col.equals("RMD_LD_LONGITUDE") || col.equals("RMD_LD_SPEED")) {
                            preparedStatement.setNull(fieldNum, java.sql.Types.NUMERIC);
                        }
                    }
                    if (col.equals("RMD_RX_SOURCE") && receivedMessageDetails.getRxSource() != null) {
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                            receivedMessageDetails.getRxSource().toString());
                    } else if (col.equals("SECURITY_RESULT_CODE")) {
                        SecurityResultCodeType securityResultCodeType = GetSecurityResultCodeTypes().stream()
                            .filter(x -> x.getSecurityResultCodeType().equals(securityResultCode.toString()))
                            .findFirst().orElse(null);
                        if (securityResultCodeType != null) {
                            preparedStatement.setInt(fieldNum, securityResultCodeType.getSecurityResultCodeTypeId());
                        } else {
                            preparedStatement.setNull(fieldNum, java.sql.Types.INTEGER);
                        }
                    }
                } else {
                    // message details are null, set all to null (with correct type)
                    if (col.equals("RMD_LD_ELEVATION") || col.equals("RMD_LD_HEADING") || col.equals("RMD_LD_LATITUDE")
                        || col.equals("RMD_LD_LONGITUDE") || col.equals("RMD_LD_SPEED")) {
                        preparedStatement.setNull(fieldNum, java.sql.Types.NUMERIC);
                    } else if (col.equals("RMD_RX_SOURCE")) {
                        preparedStatement.setString(fieldNum, null);
                    } else if (col.equals("SECURITY_RESULT_CODE")) {
                        preparedStatement.setNull(fieldNum, java.sql.Types.INTEGER);
                    }
                }

                if (col.equals("SAT_RECORD_ID")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, satRecordId);
                } else if (col.equals("TIM_NAME")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, regionName);
                } else if (col.equals("LOG_FILE_NAME")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, logFileName);
                } else if (col.equals("RECORD_TYPE") && recordType != null) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, recordType.toString());
                }
                fieldNum++;
            }
            // execute insert statement
            return dbInteractions.executeAndLog(preparedStatement, "timID");
        } catch (SQLException e) {
            log.error("Failed to insert tim into database", e);
        }
        return 0L;
    }

    /**
     * Adds regions to the database for a given DataFrame.
     *
     * @param dataFrame   The DataFrame containing the regions to be added.
     * @param dataFrameId The ID of the DataFrame.
     */
    public void addRegions(DataFrame dataFrame, Long dataFrameId) {
        for (Region region : dataFrame.getRegions()) {
            Path path = region.getPath();
            Geometry geometry = region.getGeometry();

            if (path != null) {
                Long pathId = pathService.InsertPath();
                regionService.AddRegion(dataFrameId, pathId, region);

                Long nodeXYId;
                Long nodeLLId;
                for (OdeTravelerInformationMessage.NodeXY nodeXY : path.getNodes()) {
                    if (nodeXY.getDelta().toLowerCase().contains("xy")) {
                        nodeXYId = nodeXYService.AddNodeXY(nodeXY);
                        pathNodeXYService.insertPathNodeXY(nodeXYId, pathId);
                    } else {
                        // node-LL
                        nodeLLId = nodeLLService.AddNodeLL(nodeXY);
                        pathNodeLLService.insertPathNodeLL(nodeLLId, pathId);
                    }
                }
            } else if (geometry != null) {
                regionService.AddRegion(dataFrameId, null, region);
            } else {
                log.warn("addActiveTimToDatabase - Unable to insert region, no path or geometry found (data_frame_id: {})",
                    dataFrameId);
            }
        }
    }

    public void addDataFrameItis(DataFrame dataFrame, Long dataFrameId) {
        // save DataFrame ITIS codes
        String[] items = dataFrame.getItems();
        if (items == null || items.length == 0) {
            log.warn("No itis codes found to associate with data_frame {}", dataFrameId);
            return;
        }
        for (var i = 0; i < items.length; i++) {
            var timItisCode = items[i];

            if (StringUtils.isNumeric(timItisCode)) {
                String itisCodeId = getItisCodeId(timItisCode);
                if (itisCodeId != null) {
                    dataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, itisCodeId, i);
                } else {
                    log.warn("Could not find corresponding itis code it for {}", timItisCode);
                }
            } else {
                dataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, timItisCode, i);
            }
        }
    }

    public boolean updateTimSatRecordId(Long timId, String satRecordId) {
        try (
            Connection connection = dbInteractions.getConnectionPool();
            PreparedStatement preparedStatement = connection.prepareStatement("update tim set sat_record_id = ? where tim_id = ?");
        ) {
            preparedStatement.setString(1, satRecordId);
            preparedStatement.setLong(2, timId);
            return dbInteractions.updateOrDelete(preparedStatement);
        } catch (Exception ex) {
            return false;
        }
    }

    public ActiveTim setActiveTimByRegionName(String regionName) {

        if (StringUtils.isBlank(regionName) || StringUtils.isEmpty(regionName)) {
            return null;
        }

        ActiveTim activeTim = new ActiveTim();
        RegionNameElementCollection elements = new RegionNameElementCollection(regionName);

        activeTim.setDirection(elements.direction);

        if (elements.route != null) {
            activeTim.setRoute(elements.route);
        } else {
            return activeTim;
        }
        if (elements.rsuOrSat != null) {
            // if this is an RSU TIM
            String[] hyphen_array = elements.rsuOrSat.split("-");
            if (hyphen_array.length > 1) {
                if (hyphen_array[0].equals("SAT")) {
                    activeTim.setSatRecordId(hyphen_array[1]);
                } else {
                    activeTim.setRsuTarget(hyphen_array[1]);
                }
            }
        } else {
            return activeTim;
        }
        if (elements.timType != null) {
            TimType timType = getTimType(elements.timType);
            if (timType != null) {
                activeTim.setTimType(timType.getType());
                activeTim.setTimTypeId(timType.getTimTypeId());
            }
        } else {
            return activeTim;
        }

        if (elements.timId != null) {
            activeTim.setClientId(elements.timId);
        } else {
            return activeTim;
        }

        if (elements.pk != null) {
            try {
                Integer pk = Integer.valueOf(elements.pk);
                activeTim.setPk(pk);
            } catch (NumberFormatException ex) {
                // the pk won't get set here
            }
        }

        return activeTim;
    }

    public TimType getTimType(String timTypeName) {

        return timTypeService.getTimTypes().stream().filter(x -> x.getType().equals(timTypeName)).findFirst()
            .orElse(null);
    }

    public String getItisCodeId(String item) {

        String itisCodeId = null;

        try {
            ItisCode itisCode = itisCodeService.selectAllItisCodes().stream()
                .filter(x -> x.getItisCode().equals(Integer.parseInt(item))).findFirst().orElse(null);
            if (itisCode != null) {
                itisCodeId = itisCode.getItisCodeId().toString();
            }
        } catch (Exception ex) {
            // on rare occasions we see an unparsable Integer
            log.error("Failed to parse ITIS integer({}): {}", item, ex.getMessage());
        }

        return itisCodeId;
    }

    public boolean updateActiveTimExpiration(CertExpirationModel cem) throws ParseException {
        var minExp = activeTimService.getMinExpiration(cem.getPacketID(), cem.getExpirationDate());

        return activeTimService.updateActiveTimExpiration(cem.getPacketID(), minExp);
    }

    /**
     * Helper method to get an OdeTravelerInformationMessage object given an OdeTimPayload.
     */
    private OdeTravelerInformationMessage getTim(OdeTimPayload odeTimPayload) {
        return (OdeTravelerInformationMessage) odeTimPayload.getData();
    }
}