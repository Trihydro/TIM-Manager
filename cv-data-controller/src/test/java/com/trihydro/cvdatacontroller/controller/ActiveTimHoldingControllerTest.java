package com.trihydro.cvdatacontroller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.tables.TimDbTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ActiveTimHoldingControllerTest extends TestBase<ActiveTimHoldingController> {
        @Spy
        private TimDbTables mockTimDbTables = new TimDbTables();
        @Mock
        private SQLNullHandler mockSqlNullHandler;

        private Coordinate startCoord;
        private Coordinate endCoord;

        @BeforeEach
        public void setupSubTest() {
                uut.InjectDependencies(mockTimDbTables, mockSqlNullHandler);
                startCoord = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2));
                endCoord = new Coordinate(BigDecimal.valueOf(5), BigDecimal.valueOf(6));
        }

        private void setupInsertQueryStatement() {
                doReturn("insert query statement").when(mockTimDbTables).buildInsertQueryStatement(any(), any());
        }

        private void setupPreparedStatement() throws SQLException {
                doReturn(mockPreparedStatement).when(mockConnection).prepareStatement("insert query statement",
                                new String[] { "active_tim_holding_id" });
        }

        @Test
        public void InsertActiveTimHolding_SUCCESS() throws SQLException {
                // Arrange
                setupInsertQueryStatement();
                setupPreparedStatement();
                ActiveTimHolding activeTimHolding = new ActiveTimHolding();
                activeTimHolding.setStartPoint(startCoord);
                activeTimHolding.setEndPoint(endCoord);
                activeTimHolding.setExpirationDateTime("2021-MAR-16'T'09:22'Z'");

                var now = Instant.parse(activeTimHolding.getDateCreated());
                java.util.Date date_created = java.util.Date.from(now);
                doReturn(date_created).when(mockUtility).convertDate(any());
                mockUtility.timestampFormat = timestampFormat;

                // Act
                ResponseEntity<Long> data = uut.InsertActiveTimHolding(activeTimHolding);

                // Assert
                Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, activeTimHolding.getClientId());// CLIENT_ID
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3, activeTimHolding.getDirection());// DIRECTION
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 4, activeTimHolding.getRsuTarget());// RSU_TARGET
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 5, activeTimHolding.getSatRecordId());// SAT_RECORD_ID
                verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 6,
                                activeTimHolding.getStartPoint().getLatitude());// START_LATITUDE
                verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 7,
                                activeTimHolding.getStartPoint().getLongitude());// START_LONGITUDE
                verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 8,
                                activeTimHolding.getEndPoint().getLatitude());// END_LATITUDE
                verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 9,
                                activeTimHolding.getEndPoint().getLongitude());// END_LONGITUDE
                verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 10, activeTimHolding.getRsuIndex());// RSU_INDEX
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 11,
                                timestampFormat.format(date_created));// DATE_CREATED
                verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 12,
                                activeTimHolding.getProjectKey());// PROJECT_KEY
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 13,
                                timestampFormat.format(date_created));// EXPIRATION_DATE
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 14, activeTimHolding.getPacketId());// PACKET_ID
        }

        @Test
        public void InsertActiveTimHolding_ExistingSDX() throws SQLException {
                // Arrange
                setupInsertQueryStatement();
                setupPreparedStatement();
                ActiveTimHolding activeTimHolding = new ActiveTimHolding();
                activeTimHolding.setSatRecordId("satRecordId");
                activeTimHolding.setClientId("clientId");
                activeTimHolding.setDirection("direction");
                activeTimHolding.setStartPoint(startCoord);
                activeTimHolding.setEndPoint(endCoord);
                doReturn(null).when(mockDbInteractions).executeAndLog(mockPreparedStatement, "active tim holding");
                doReturn(-99l).when(mockRs).getLong("ACTIVE_TIM_HOLDING_ID");

                var now = Instant.parse(activeTimHolding.getDateCreated());
                java.util.Date date_created = java.util.Date.from(now);
                doReturn(date_created).when(mockUtility).convertDate(activeTimHolding.getDateCreated());
                mockUtility.timestampFormat = timestampFormat;

                String query = "select active_tim_holding_id from active_tim_holding";
                query += " where sat_record_id = '" + activeTimHolding.getSatRecordId();
                query += "' and client_id = '" + activeTimHolding.getClientId();
                query += "' and direction = '" + activeTimHolding.getDirection() + "'";

                // Act
                ResponseEntity<Long> data = uut.InsertActiveTimHolding(activeTimHolding);

                // Assert
                Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
                Assertions.assertEquals(Long.valueOf(-99), data.getBody());
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, activeTimHolding.getClientId());// CLIENT_ID
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3, activeTimHolding.getDirection());// DIRECTION
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 4, activeTimHolding.getRsuTarget());// RSU_TARGET
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 5, activeTimHolding.getSatRecordId());// SAT_RECORD_ID
                verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 6,
                                activeTimHolding.getStartPoint().getLatitude());// START_LATITUDE
                verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 7,
                                activeTimHolding.getStartPoint().getLongitude());// START_LONGITUDE
                verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 8,
                                activeTimHolding.getEndPoint().getLatitude());// END_LATITUDE
                verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 9,
                                activeTimHolding.getEndPoint().getLongitude());// END_LONGITUDE
                verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 10, activeTimHolding.getRsuIndex());// RSU_INDEX
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 11,
                                timestampFormat.format(date_created));// DATE_CREATED

                verify(mockStatement).executeQuery(query);
        }

        @Test
        public void InsertActiveTimHolding_ExistingRSU() throws SQLException {
                // Arrange
                setupInsertQueryStatement();
                setupPreparedStatement();
                ActiveTimHolding activeTimHolding = new ActiveTimHolding();
                activeTimHolding.setRsuTargetId("10.10.10.1");
                activeTimHolding.setClientId("clientId");
                activeTimHolding.setDirection("direction");
                activeTimHolding.setStartPoint(startCoord);
                activeTimHolding.setEndPoint(endCoord);
                doReturn(null).when(mockDbInteractions).executeAndLog(mockPreparedStatement, "active tim holding");
                doReturn(-99l).when(mockRs).getLong("ACTIVE_TIM_HOLDING_ID");

                var now = Instant.parse(activeTimHolding.getDateCreated());
                java.util.Date date_created = java.util.Date.from(now);
                doReturn(date_created).when(mockUtility).convertDate(activeTimHolding.getDateCreated());
                mockUtility.timestampFormat = timestampFormat;

                String query = "select active_tim_holding_id from active_tim_holding";
                query += " where rsu_target = '" + activeTimHolding.getRsuTarget();
                query += "' and client_id = '" + activeTimHolding.getClientId();
                query += "' and direction = '" + activeTimHolding.getDirection() + "'";

                // Act
                ResponseEntity<Long> data = uut.InsertActiveTimHolding(activeTimHolding);

                // Assert
                Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
                Assertions.assertEquals(Long.valueOf(-99), data.getBody());
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, activeTimHolding.getClientId());// CLIENT_ID
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3, activeTimHolding.getDirection());// DIRECTION
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 4, activeTimHolding.getRsuTarget());// RSU_TARGET
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 5, activeTimHolding.getSatRecordId());// SAT_RECORD_ID
                verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 6,
                                activeTimHolding.getStartPoint().getLatitude());// START_LATITUDE
                verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 7,
                                activeTimHolding.getStartPoint().getLongitude());// START_LONGITUDE
                verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 8,
                                activeTimHolding.getEndPoint().getLatitude());// END_LATITUDE
                verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 9,
                                activeTimHolding.getEndPoint().getLongitude());// END_LONGITUDE
                verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 10, activeTimHolding.getRsuIndex());// RSU_INDEX
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 11,
                                timestampFormat.format(date_created));// DATE_CREATED

                verify(mockStatement).executeQuery(query);
        }

        @Test
        public void InsertActiveTimHolding_FAIL() throws SQLException {
                // Arrange
                setupInsertQueryStatement();
                setupPreparedStatement();
                ActiveTimHolding activeTimHolding = new ActiveTimHolding();
                activeTimHolding.setStartPoint(startCoord);
                activeTimHolding.setEndPoint(endCoord);
                doThrow(new SQLException()).when(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2,
                                activeTimHolding.getClientId());

                // Act
                ResponseEntity<Long> data = uut.InsertActiveTimHolding(activeTimHolding);

                // Assert
                Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
                verify(mockPreparedStatement).close();
                verify(mockConnection).close();

        }

        @Test
        public void getActiveTimHoldingForRsu_SUCCESS() throws SQLException {
                // Arrange

                // Act
                ResponseEntity<List<ActiveTimHolding>> data = uut.getActiveTimHoldingForRsu("ipv4Address");

                // Assert
                Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
                Assertions.assertNotNull(data.getBody());
                Assertions.assertEquals(1, data.getBody().size());
                verify(mockRs).getLong("ACTIVE_TIM_HOLDING_ID");
                verify(mockRs).getString("CLIENT_ID");
                verify(mockRs).getString("DIRECTION");
                verify(mockRs).getString("RSU_TARGET");
                verify(mockRs).getString("SAT_RECORD_ID");
                verify(mockRs).getBigDecimal("START_LATITUDE");
                verify(mockRs).getBigDecimal("START_LONGITUDE");
                verify(mockRs).getBigDecimal("END_LATITUDE");
                verify(mockRs).getBigDecimal("END_LONGITUDE");
                verify(mockRs).getString("DATE_CREATED");
                verify(mockRs).getInt("RSU_INDEX");
                verify(mockStatement).close();
                verify(mockConnection).close();
                verify(mockRs).close();
        }

        @Test
        public void getActiveTimHoldingForRsu_FAIL() throws SQLException {
                // Arrange
                doThrow(new SQLException()).when(mockRs).getLong("ACTIVE_TIM_HOLDING_ID");

                // Act
                ResponseEntity<List<ActiveTimHolding>> data = uut.getActiveTimHoldingForRsu("ipv4Address");

                // Assert
                Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
                Assertions.assertNotNull(data.getBody());
                Assertions.assertEquals(0, data.getBody().size());
                verify(mockStatement).close();
                verify(mockConnection).close();
                verify(mockRs).close();
        }
}