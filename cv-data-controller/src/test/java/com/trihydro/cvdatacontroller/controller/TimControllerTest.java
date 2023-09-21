package com.trihydro.cvdatacontroller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.model.TimInsertModel;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;
import com.trihydro.library.tables.TimDbTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeLogMsgMetadataLocation;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.RxSource;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

public class TimControllerTest extends TestBase<TimController> {

        @Mock
        private SQLNullHandler mockSqlNullHandler;
        @Spy
        private TimDbTables mockTimDbTables;
        @Mock
        private SecurityResultCodeTypeController mockSecurityResultCodeTypeController;
        @Mock
        private ResponseEntity<List<SecurityResultCodeType>> mockResponseEntitySecurityResultCodeTypeList;

        @BeforeEach
        public void setupSubTest() {
                uut.InjectDependencies(mockTimDbTables, mockSqlNullHandler, mockSecurityResultCodeTypeController);
        }

        private void setupInsertQueryStatement() {
                doReturn("").when(mockTimDbTables).buildInsertQueryStatement(any(), any());
        }

        private void setupSecurityResultTypes() {
                List<SecurityResultCodeType> secResultCodeTypes = new ArrayList<>();
                SecurityResultCodeType srct = new SecurityResultCodeType();
                srct.setSecurityResultCodeType(SecurityResultCode.success.toString());
                srct.setSecurityResultCodeTypeId(-1);
                secResultCodeTypes.add(srct);
                doReturn(secResultCodeTypes).when(mockResponseEntitySecurityResultCodeTypeList).getBody();
                when(mockSecurityResultCodeTypeController.GetSecurityResultCodeTypes())
                                .thenReturn(mockResponseEntitySecurityResultCodeTypeList);
        }

        @Test
        public void AddTim_J2735_SUCCESS() throws SQLException {
                // Arrange
                setupInsertQueryStatement();
                TimInsertModel tim = new TimInsertModel();
                tim.setJ2735TravelerInformationMessage(new OdeTravelerInformationMessage());
                tim.setRecordType(RecordType.driverAlert);
                tim.setLogFileName("logFileName");
                tim.setSecurityResultCode(SecurityResultCode.success);
                tim.setSatRecordId("recordId");
                tim.setRegionName("regionName");
                OdeTravelerInformationMessage j2735 = tim.getJ2735TravelerInformationMessage();

                // Act
                Long timId = uut.AddTim(tim);

                // Assert
                Assertions.assertEquals(Long.valueOf(-1), timId);
                verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 1, j2735.getMsgCnt());
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, j2735.getPacketID());
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3, j2735.getUrlB());
                verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 4, null);
                verify(mockPreparedStatement).close();
                verify(mockConnection).close();
        }

        @Test
        public void AddTim_timMetadata_SUCCESS() throws SQLException {
                // Arrange
                setupInsertQueryStatement();
                TimInsertModel tim = new TimInsertModel();
                OdeMsgMetadata omm = GetOmm();
                omm.setRecordGeneratedBy(GeneratedBy.TMC);
                tim.setOdeTimMetadata(omm);
                tim.setRecordType(RecordType.driverAlert);
                tim.setLogFileName("LOGFILENAME");
                tim.setSecurityResultCode(SecurityResultCode.success);
                tim.setSatRecordId("RECORDID");
                tim.setRegionName("REGIONNAME");
                OdeMsgMetadata odeTimMetadata = tim.getOdeTimMetadata();

                var genTime = Instant.parse(tim.getOdeTimMetadata().getRecordGeneratedAt());
                var recTime = Instant.parse(tim.getOdeTimMetadata().getOdeReceivedAt());
                java.util.Date gen_at = java.util.Date.from(genTime);
                java.util.Date rec_at = java.util.Date.from(recTime);
                doReturn(gen_at).when(mockUtility).convertDate(tim.getOdeTimMetadata().getRecordGeneratedAt());
                doReturn(rec_at).when(mockUtility).convertDate(tim.getOdeTimMetadata().getOdeReceivedAt());
                mockUtility.timestampFormat = timestampFormat;

                // Act
                Long timId = uut.AddTim(tim);

                // Assert
                // j2735 fields are skipped, we start at index 5 after those
                // See timDbTables.getTimTable() for ordering
                Assertions.assertEquals(Long.valueOf(-1), timId);
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 5,
                                odeTimMetadata.getRecordGeneratedBy().toString());// RECORD_GENERATED_BY
                verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 12,
                                odeTimMetadata.getSchemaVersion());// SCHEMA_VERSION
                verify(mockPreparedStatement).setString(1, null);// RECORD_GENERATED_AT
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 17,
                                odeTimMetadata.getSerialId().getStreamId());// SERIAL_ID_STREAM_ID
                verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 18,
                                odeTimMetadata.getSerialId().getBundleSize());// SERIAL_ID_BUNDLE_SIZE
                verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 19,
                                odeTimMetadata.getSerialId().getBundleId());// SERIAL_ID_BUNDLE_ID
                verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 20,
                                odeTimMetadata.getSerialId().getRecordId());// SERIAL_ID_RECORD_ID
                verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 21,
                                odeTimMetadata.getSerialId().getSerialNumber());// SERIAL_ID_SERIAL_NUMBER
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 22, odeTimMetadata.getPayloadType());// PAYLOAD_TYPE
                verify(mockPreparedStatement).close();
                verify(mockConnection).close();
        }

        @Test
        public void AddTim_receivedMessageDetails_SUCCESS() throws SQLException {
                // Arrange
                setupInsertQueryStatement();
                setupSecurityResultTypes();
                TimInsertModel tim = new TimInsertModel();
                tim.setReceivedMessageDetails(getRxMsg());
                tim.setRecordType(RecordType.driverAlert);
                tim.setLogFileName("LOGFILENAME");
                tim.setSecurityResultCode(SecurityResultCode.success);
                tim.setSatRecordId("RECORDID");
                tim.setRegionName("REGIONNAME");
                ReceivedMessageDetails receivedMessageDetails = tim.getReceivedMessageDetails();

                // Act
                Long timId = uut.AddTim(tim);

                // Assert
                // See timDbTables.getTimTable() for ordering
                Assertions.assertEquals(Long.valueOf(-1), timId);
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 6,
                                Double.parseDouble(receivedMessageDetails.getLocationData().getElevation()));// RMD_LD_ELEVATION
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 7,
                                Double.parseDouble(receivedMessageDetails.getLocationData().getHeading()));// RMD_LD_HEADING
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 8,
                                Double.parseDouble(receivedMessageDetails.getLocationData().getLatitude()));// RMD_LD_LATITUDE
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 9,
                                Double.parseDouble(receivedMessageDetails.getLocationData().getLongitude()));// RMD_LD_LONGITUDE
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 10,
                                Double.parseDouble(receivedMessageDetails.getLocationData().getSpeed()));// RMD_LD_SPEED
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 11,
                                receivedMessageDetails.getRxSource().toString());// RMD_RX_SOURCE
                verify(mockPreparedStatement).setInt(13, -1);// SECURITY_RESULT_CODE
                verify(mockPreparedStatement).close();
                verify(mockConnection).close();
        }

        @Test
        public void GetTim_SUCCESS() throws SQLException {
                // Arrange
                Long timId = -1l;
                String selectStatement = "select * from tim where tim_id = " + timId;

                // Act
                ResponseEntity<WydotOdeTravelerInformationMessage> data = uut.GetTim(timId);

                // Assert
                Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
                verify(mockStatement).executeQuery(selectStatement);
                verify(mockRs).getString("PACKET_ID");
                verify(mockRs).getInt("MSG_CNT");
                verify(mockRs).getString("TIME_STAMP");
                verify(mockRs).getString("URL_B");
                verify(mockStatement).close();
                verify(mockConnection).close();
                verify(mockRs).close();
        }

        @Test
        public void GetTim_FAIL() throws SQLException {
                // Arrange
                Long timId = -1l;
                String selectStatement = "select * from tim where tim_id = " + timId;
                doThrow(new SQLException()).when(mockRs).getString("PACKET_ID");

                // Act
                ResponseEntity<WydotOdeTravelerInformationMessage> data = uut.GetTim(timId);

                // Assert
                Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
                verify(mockStatement).executeQuery(selectStatement);
                verify(mockStatement).close();
                verify(mockConnection).close();
                verify(mockRs).close();
        }

        @Test
        public void deleteOldTim() throws SQLException {
                // Arrange
                String strDate = uut.getOneMonthPrior();
                doReturn(strDate).when(uut).getOneMonthPrior();
                Timestamp timestamp = null;
                try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
                        Date parsedDate = dateFormat.parse(strDate);
                        timestamp = new java.sql.Timestamp(parsedDate.getTime());
                } catch (ParseException e) {
                        e.printStackTrace();
                        Assertions.fail("Failed to parse date");
                }

                // Act
                var data = uut.deleteOldTim();

                // Assert
                Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
                Assertions.assertTrue(data.getBody(), "Fail return on success");

                verify(uut, times(2)).getOneMonthPrior();

                String deleteTimRsuSQL = "DELETE FROM tim_rsu WHERE tim_id IN";

                String deleteDfItis = "DELETE FROM DATA_FRAME_ITIS_CODE where data_frame_id in";
                deleteDfItis += " (select data_frame_id from data_frame WHERE tim_id IN";

                String deleteNodeLL = "DELETE FROM node_ll WHERE node_ll_id IN";
                deleteNodeLL += " (SELECT node_ll_id from path_node_ll WHERE path_id in (SELECT path_id from region where data_frame_id in";
                deleteNodeLL += " (select data_frame_id from data_frame WHERE tim_id IN";

                String deletePathNodeLL = "DELETE FROM path_node_ll WHERE path_id in (SELECT path_id from region where data_frame_id in";
                deletePathNodeLL += " (select data_frame_id from data_frame WHERE tim_id IN";

                String deletePath = "DELETE FROM path WHERE path_id in (SELECT path_id from region where data_frame_id in";
                deletePath += " (select data_frame_id from data_frame WHERE tim_id IN";

                String deleteRegion = "DELETE FROM region where data_frame_id in";
                deleteRegion += " (select data_frame_id from data_frame WHERE tim_id IN";

                String deleteDataFrame = "DELETE FROM data_frame WHERE tim_id IN";

                String deleteTim = "DELETE FROM tim WHERE ode_received_at < ? and tim_id NOT IN (SELECT tim_id FROM active_tim)";
                String deleteSQL = " (SELECT tim_id FROM tim WHERE ode_received_at < ? AND tim_id NOT IN (SELECT tim_id FROM active_tim))";

                deleteTimRsuSQL += deleteSQL;
                deleteDfItis += deleteSQL + ")";
                deleteNodeLL += deleteSQL + ")))";
                deletePathNodeLL += deleteSQL + "))";
                deletePath += deleteSQL + "))";
                deleteRegion += deleteSQL + ")";
                deleteDataFrame += deleteSQL;

                verify(mockConnection).prepareStatement(deleteTimRsuSQL);
                verify(mockConnection).prepareStatement(deleteDfItis);
                verify(mockConnection).prepareStatement(deleteNodeLL);
                verify(mockConnection).prepareStatement(deletePathNodeLL);
                verify(mockConnection).prepareStatement(deletePath);
                verify(mockConnection).prepareStatement(deleteRegion);
                verify(mockConnection).prepareStatement(deleteDataFrame);
                verify(mockConnection).prepareStatement(deleteTim);

                verify(mockPreparedStatement, times(8)).setTimestamp(1, timestamp);
                verify(mockPreparedStatement, times(8)).close();
                verify(mockConnection, times(8)).close();
        }

        private ReceivedMessageDetails getRxMsg() {
                ReceivedMessageDetails rxMsg = new ReceivedMessageDetails();
                
                OdeLogMsgMetadataLocation locationData = new OdeLogMsgMetadataLocation();
                locationData.setElevation("1.0");
                locationData.setHeading("2.0");
                locationData.setLatitude("3.0");
                locationData.setLongitude("4.0");
                locationData.setSpeed("5.0");
                rxMsg.setLocationData(locationData);

                rxMsg.setRxSource(RxSource.SNMP);
                return rxMsg;
        }

        private OdeMsgMetadata GetOmm() {
                OdeMsgMetadata omm = new OdeMsgMetadata();
                omm.setRecordGeneratedAt("2020-02-03T16:02:00.000Z");
                omm.setOdeReceivedAt("2020-02-03T16:00:00.000Z");
                return omm;
        }
}