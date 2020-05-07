package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(StrictStubs.class)
public class ActiveTimHoldingControllerTest extends TestBase<ActiveTimHoldingController> {
        @Spy
        private TimOracleTables mockTimOracleTables = new TimOracleTables();
        @Mock
        private SQLNullHandler mockSqlNullHandler;

        @Before
        public void setupSubTest() throws SQLException {
                uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
                doReturn("insert query statement").when(mockTimOracleTables).buildInsertQueryStatement(any(), any());
                doReturn(mockPreparedStatement).when(mockConnection).prepareStatement("insert query statement",
                                new String[] { "active_tim_holding_id" });
        }

        @Test
        public void InsertActiveTimHolding_SUCCESS() throws SQLException {
                // Arrange
                ActiveTimHolding activeTimHolding = new ActiveTimHolding();
                activeTimHolding.setStartPoint(new Coordinate(1, 2));
                activeTimHolding.setEndPoint(new Coordinate(5, 6));

                // Act
                ResponseEntity<Long> data = uut.InsertActiveTimHolding(activeTimHolding);

                // Assert
                assertEquals(HttpStatus.OK, data.getStatusCode());
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, activeTimHolding.getClientId());// CLIENT_ID
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3, activeTimHolding.getDirection());// DIRECTION
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 4, activeTimHolding.getRsuTarget());// RSU_TARGET
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 5, activeTimHolding.getSatRecordId());// SAT_RECORD_ID
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 6,
                                activeTimHolding.getStartPoint().getLatitude());// START_LATITUDE
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 7,
                                activeTimHolding.getStartPoint().getLongitude());// START_LONGITUDE
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 8,
                                activeTimHolding.getEndPoint().getLatitude());// END_LATITUDE
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 9,
                                activeTimHolding.getEndPoint().getLongitude());// END_LONGITUDE
                verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 10, activeTimHolding.getRsuIndex());// RSU_INDEX
                verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 11,
                                java.sql.Timestamp.valueOf(LocalDateTime.parse(activeTimHolding.getDateCreated(),
                                                DateTimeFormatter.ISO_DATE_TIME)));// DATE_CREATED
        }

        @Test
        public void InsertActiveTimHolding_ExistingSDX() throws SQLException {
                // Arrange
                ActiveTimHolding activeTimHolding = new ActiveTimHolding();
                activeTimHolding.setSatRecordId("satRecordId");
                activeTimHolding.setClientId("clientId");
                activeTimHolding.setDirection("direction");
                activeTimHolding.setStartPoint(new Coordinate(1, 2));
                activeTimHolding.setEndPoint(new Coordinate(5, 6));
                doReturn(null).when(mockDbInteractions).executeAndLog(mockPreparedStatement, "active tim holding");
                doReturn(-99l).when(mockRs).getLong("ACTIVE_TIM_HOLDING_ID");

                String query = "select active_tim_holding_id from active_tim_holding";
                query += " where sat_record_id = '" + activeTimHolding.getSatRecordId();
                query += "' and client_id = '" + activeTimHolding.getClientId();
                query += "' and direction = '" + activeTimHolding.getDirection() + "'";

                // Act
                ResponseEntity<Long> data = uut.InsertActiveTimHolding(activeTimHolding);

                // Assert
                assertEquals(HttpStatus.OK, data.getStatusCode());
                assertEquals(Long.valueOf(-99), data.getBody());
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, activeTimHolding.getClientId());// CLIENT_ID
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3, activeTimHolding.getDirection());// DIRECTION
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 4, activeTimHolding.getRsuTarget());// RSU_TARGET
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 5, activeTimHolding.getSatRecordId());// SAT_RECORD_ID
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 6,
                                activeTimHolding.getStartPoint().getLatitude());// START_LATITUDE
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 7,
                                activeTimHolding.getStartPoint().getLongitude());// START_LONGITUDE
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 8,
                                activeTimHolding.getEndPoint().getLatitude());// END_LATITUDE
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 9,
                                activeTimHolding.getEndPoint().getLongitude());// END_LONGITUDE
                verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 10, activeTimHolding.getRsuIndex());// RSU_INDEX
                verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 11,
                                java.sql.Timestamp.valueOf(LocalDateTime.parse(activeTimHolding.getDateCreated(),
                                                DateTimeFormatter.ISO_DATE_TIME)));// DATE_CREATED

                verify(mockStatement).executeQuery(query);
        }

        @Test
        public void InsertActiveTimHolding_ExistingRSU() throws SQLException {
                // Arrange
                ActiveTimHolding activeTimHolding = new ActiveTimHolding();
                activeTimHolding.setRsuTargetId("10.10.10.1");
                activeTimHolding.setClientId("clientId");
                activeTimHolding.setDirection("direction");
                activeTimHolding.setStartPoint(new Coordinate(1, 2));
                activeTimHolding.setEndPoint(new Coordinate(5, 6));
                doReturn(null).when(mockDbInteractions).executeAndLog(mockPreparedStatement, "active tim holding");
                doReturn(-99l).when(mockRs).getLong("ACTIVE_TIM_HOLDING_ID");

                String query = "select active_tim_holding_id from active_tim_holding";
                query += " where rsu_target = '" + activeTimHolding.getRsuTarget();
                query += "' and client_id = '" + activeTimHolding.getClientId();
                query += "' and direction = '" + activeTimHolding.getDirection() + "'";

                // Act
                ResponseEntity<Long> data = uut.InsertActiveTimHolding(activeTimHolding);

                // Assert
                assertEquals(HttpStatus.OK, data.getStatusCode());
                assertEquals(Long.valueOf(-99), data.getBody());
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, activeTimHolding.getClientId());// CLIENT_ID
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3, activeTimHolding.getDirection());// DIRECTION
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 4, activeTimHolding.getRsuTarget());// RSU_TARGET
                verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 5, activeTimHolding.getSatRecordId());// SAT_RECORD_ID
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 6,
                                activeTimHolding.getStartPoint().getLatitude());// START_LATITUDE
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 7,
                                activeTimHolding.getStartPoint().getLongitude());// START_LONGITUDE
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 8,
                                activeTimHolding.getEndPoint().getLatitude());// END_LATITUDE
                verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 9,
                                activeTimHolding.getEndPoint().getLongitude());// END_LONGITUDE
                verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 10, activeTimHolding.getRsuIndex());// RSU_INDEX
                verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 11,
                                java.sql.Timestamp.valueOf(LocalDateTime.parse(activeTimHolding.getDateCreated(),
                                                DateTimeFormatter.ISO_DATE_TIME)));// DATE_CREATED

                verify(mockStatement).executeQuery(query);
        }

        @Test
        public void InsertActiveTimHolding_FAIL() throws SQLException {
                // Arrange
                ActiveTimHolding activeTimHolding = new ActiveTimHolding();
                activeTimHolding.setStartPoint(new Coordinate(1, 2));
                activeTimHolding.setEndPoint(new Coordinate(5, 6));
                doThrow(new SQLException()).when(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2,
                                activeTimHolding.getClientId());

                // Act
                ResponseEntity<Long> data = uut.InsertActiveTimHolding(activeTimHolding);

                // Assert
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
                verify(mockPreparedStatement).close();
                verify(mockConnection).close();

        }

        @Test
        public void getActiveTimHoldingForRsu_SUCCESS() throws SQLException {
                // Arrange

                // Act
                ResponseEntity<List<ActiveTimHolding>> data = uut.getActiveTimHoldingForRsu("ipv4Address");

                // Assert
                assertEquals(HttpStatus.OK, data.getStatusCode());
                assertNotNull(data.getBody());
                assertEquals(1, data.getBody().size());
                verify(mockRs).getLong("ACTIVE_TIM_HOLDING_ID");
                verify(mockRs).getString("CLIENT_ID");
                verify(mockRs).getString("DIRECTION");
                verify(mockRs).getString("RSU_TARGET");
                verify(mockRs).getString("SAT_RECORD_ID");
                verify(mockRs).getDouble("START_LATITUDE");
                verify(mockRs).getDouble("START_LONGITUDE");
                verify(mockRs).getDouble("END_LATITUDE");
                verify(mockRs).getDouble("END_LONGITUDE");
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
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
                assertNotNull(data.getBody());
                assertEquals(0, data.getBody().size());
                verify(mockStatement).close();
                verify(mockConnection).close();
                verify(mockRs).close();
        }
}