package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.trihydro.cvdatacontroller.tables.TimOracleTables;
import com.trihydro.library.model.TimUpdateModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActiveTimControllerTest {
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockRs;
    @Mock
    private TimOracleTables mockTimOracleTables;

    // @Spy
    private ActiveTimController uut;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() throws SQLException {
        uut = spy(new ActiveTimController(mockTimOracleTables));
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(isA(String.class))).thenReturn(mockPreparedStatement);
        doReturn(mockConnection).when(uut).GetConnectionPool();
        when(mockStatement.executeQuery(isA(String.class))).thenReturn(mockRs);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true).thenReturn(false);

        doReturn(mockPreparedStatement).when(mockTimOracleTables).buildUpdateStatement(any(), any(), any(), any(),
                any());
    }

    @Test
    public void getExpiringActiveTims() throws SQLException {
        // Arrange
        // we only set one property to verify its returned
        when(mockRs.getLong("ACTIVE_TIM_ID")).thenReturn(999l);

        // Act
        List<TimUpdateModel> tums = uut.getExpiringActiveTims();

        // Assert
        assertEquals(1, tums.size());
        assertEquals(new Long(999), tums.get(0).getActiveTimId());
    }

    @Test
    public void updateActiveTim_SatRecordId_FAIL() {
        // Arrange
        doReturn(false).when(uut).updateOrDelete(mockPreparedStatement);

        // Act
        boolean success = uut.updateActiveTim_SatRecordId(-1l, "asdf");

        // Assert
        assertFalse("UpdateActiveTim_SatRecordId succeeded when it should have failed", success);
    }

    @Test
    public void updateActiveTim_SatRecordId_SUCCESS() {
        // Arrange
        doReturn(true).when(uut).updateOrDelete(mockPreparedStatement);

        // Act
        boolean success = uut.updateActiveTim_SatRecordId(-1l, "asdf");

        // Assert
        assertTrue("UpdateActiveTim_SatRecordId failed when it should have succeeded", success);
    }
}