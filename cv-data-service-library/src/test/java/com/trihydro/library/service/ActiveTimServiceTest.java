package com.trihydro.library.service;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.ActiveTim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.mockito.Matchers.isA;
import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbUtility.class })
public class ActiveTimServiceTest {
    // @InjectMocks
    // ActiveTimService unitUnderTest;

    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockRs;

    @Before
    public void setup() throws SQLException {
        PowerMockito.mockStatic(DbUtility.class);
        Mockito.when(DbUtility.getConnectionPool()).thenReturn(mockConnection);
        Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);
        Mockito.when(mockStatement.executeQuery(isA(String.class))).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(true).thenReturn(false);

        setupBasicActiveTimDatabaseReturn();
    }

    private void setupBasicActiveTimDatabaseReturn() throws SQLException {
        Mockito.when(mockRs.getLong("TIM_ID")).thenReturn(1l);
        Mockito.when(mockRs.getDouble("MILEPOST_START")).thenReturn(1d);
        Mockito.when(mockRs.getDouble("MILEPOST_STOP")).thenReturn(2d);
        Mockito.when(mockRs.getString("DIRECTION")).thenReturn("both");
        Mockito.when(mockRs.getString("ROUTE")).thenReturn("I 80");
        Mockito.when(mockRs.getString("CLIENT_ID")).thenReturn("123");
        Mockito.when(mockRs.getString("SAT_RECORD_ID")).thenReturn("HEX");
        Mockito.when(mockRs.getLong("ACTIVE_TIM_ID")).thenReturn(1l);
    }

    @Test
    public void getActiveTimsMissingItisCodes() throws SQLException {
        List<ActiveTim> ats = ActiveTimService.getActiveTimsMissingItisCodes();
        assertEquals(1, ats.size());
        ActiveTim tim = ats.get(0);
        assertEquals((long) tim.getTimId(), 1l);
        assertEquals((double) tim.getMilepostStart(), 1d, 0);
        assertEquals((double) tim.getMilepostStop(), 2d, 0);
        assertEquals(tim.getDirection(), "both");
        assertEquals(tim.getRoute(), "I 80");
        assertEquals(tim.getClientId(), "123");
        assertEquals(tim.getSatRecordId(), "HEX");
        assertEquals((long) tim.getActiveTimId(), 1l);
    }

    @Test
    public void getActiveTimsNotSent() {
        List<ActiveTim> ats = ActiveTimService.getActiveTimsNotSent();
        assertEquals(1, ats.size());
        ActiveTim tim = ats.get(0);
        assertEquals((long) tim.getTimId(), 1l);
        assertEquals((double) tim.getMilepostStart(), 1d, 0);
        assertEquals((double) tim.getMilepostStop(), 2d, 0);
        assertEquals(tim.getDirection(), "both");
        assertEquals(tim.getRoute(), "I 80");
        assertEquals(tim.getClientId(), "123");
        assertEquals(tim.getSatRecordId(), "HEX");
        assertEquals((long) tim.getActiveTimId(), 1l);
    }
}