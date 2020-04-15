package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.model.TimInsertModel;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
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

@RunWith(StrictStubs.class)
public class TimControllerTest extends TestBase<TimController> {

        @Mock
        private SQLNullHandler mockSqlNullHandler;
        @Spy
        private TimOracleTables mockTimOracleTables;
        @Mock
        private SecurityResultCodeTypeController mockSecurityResultCodeTypeController;
        @Mock
        private ResponseEntity<List<SecurityResultCodeType>> mockResponseEntitySecurityResultCodeTypeList;

        private String mstFormatedDate = "03-Feb-20 04.00.00.000 PM";

        @Before
        public void setupSubTest() {
                List<SecurityResultCodeType> secResultCodeTypes = new ArrayList<>();
                SecurityResultCodeType srct = new SecurityResultCodeType();
                srct.setSecurityResultCodeType(SecurityResultCode.success.toString());
                srct.setSecurityResultCodeTypeId(-1);
                secResultCodeTypes.add(srct);

                doReturn("").when(mockTimOracleTables).buildInsertQueryStatement(any(), any());
                doReturn(secResultCodeTypes).when(mockResponseEntitySecurityResultCodeTypeList).getBody();
                when(mockSecurityResultCodeTypeController.GetSecurityResultCodeTypes())
                                .thenReturn(mockResponseEntitySecurityResultCodeTypeList);
                uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler, mockSecurityResultCodeTypeController);
        }

        @Test
        public void AddTim_J2735_SUCCESS() throws SQLException {
                // Arrange
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
                assertEquals(new Long(-1), timId);
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

                // Act
                Long timId = uut.AddTim(tim);

                // Assert
                // j2735 fields are skipped, we start at index 5 after those
                // See timOracleTables.getTimTable() for ordering
                assertEquals(new Long(-1), timId);
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 5,
                                odeTimMetadata.getRecordGeneratedBy().toString());// RECORD_GENERATED_BY
                verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 12,
                                odeTimMetadata.getSchemaVersion());// SCHEMA_VERSION
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 15, mstFormatedDate);// RECORD_GENERATED_AT
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
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 24, mstFormatedDate);// ODE_RECEIVED_AT
                verify(mockPreparedStatement).close();
                verify(mockConnection).close();
        }

        @Test
        public void AddTim_receivedMessageDetails_SUCCESS() throws SQLException {
                // Arrange
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
                // See timOracleTables.getTimTable() for ordering
                assertEquals(new Long(-1), timId);
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 6,
                                receivedMessageDetails.getLocationData().getElevation());// RMD_LD_ELEVATION
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 7,
                                receivedMessageDetails.getLocationData().getHeading());// RMD_LD_HEADING
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 8,
                                receivedMessageDetails.getLocationData().getLatitude());// RMD_LD_LATITUDE
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 9,
                                receivedMessageDetails.getLocationData().getLongitude());// RMD_LD_LONGITUDE
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 10,
                                receivedMessageDetails.getLocationData().getSpeed());// RMD_LD_SPEED
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
                assertEquals(HttpStatus.OK, data.getStatusCode());
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
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
                verify(mockStatement).executeQuery(selectStatement);
                verify(mockStatement).close();
                verify(mockConnection).close();
                verify(mockRs).close();
        }

        private ReceivedMessageDetails getRxMsg() {
                ReceivedMessageDetails rxMsg = new ReceivedMessageDetails();
                rxMsg.setLocationData(new OdeLogMsgMetadataLocation());
                rxMsg.setRxSource(RxSource.SNMP);
                return rxMsg;
        }

        private OdeMsgMetadata GetOmm() {
                OdeMsgMetadata omm = new OdeMsgMetadata();
                omm.setRecordGeneratedAt("2020-02-03T16:00:00.000Z");
                omm.setOdeReceivedAt("2020-02-03T16:00:00.000Z");
                return omm;
        }
}