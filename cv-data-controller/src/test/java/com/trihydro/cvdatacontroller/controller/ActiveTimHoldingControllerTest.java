package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class ActiveTimHoldingControllerTest extends TestBase<ActiveTimHoldingController> {
        @Spy
        private TimOracleTables mockTimOracleTables = new TimOracleTables();
        @Mock
        private SQLNullHandler mockSqlNullHandler;
        @Mock
        private Utility mockUtility;

        @Before
        public void setupSubTest() throws SQLException {
                uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler, mockUtility);
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

}