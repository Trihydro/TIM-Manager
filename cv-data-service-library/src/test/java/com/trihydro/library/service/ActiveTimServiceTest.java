package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.WydotTim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbUtility.class, ActiveTimService.class, RestTemplateProvider.class })
public class ActiveTimServiceTest {
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockRs;
    @Mock
    private SQLException sqlException;
    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    private ResponseEntity<TimUpdateModel[]> mockResponseEntity;

    private Long timTypeId = -1l;
    private List<WydotTim> wydotTims;

    @Before
    public void setup() throws SQLException {
        PowerMockito.mockStatic(DbUtility.class);
        PowerMockito.mockStatic(RestTemplateProvider.class);
        when(DbUtility.getConnectionPool()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(isA(String.class))).thenReturn(mockPreparedStatement);
        when(mockStatement.executeQuery(isA(String.class))).thenReturn(mockRs);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true).thenReturn(false);
        when(RestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);

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

    private void setupWydotTims() {
        wydotTims = new ArrayList<>();
        WydotTim wydotTim = new WydotTim();
        wydotTim.setDirection("westbound");
        wydotTim.setClientId("unit_test_id1");
        wydotTims.add(wydotTim);
        wydotTim = new WydotTim();
        wydotTim.setDirection("eastbound");
        wydotTim.setClientId("unit_test_id2");
        wydotTims.add(wydotTim);
    }

    @Test
    public void getActiveTimsMissingItisCodes() throws SQLException {
        // Arrange
        TimUpdateModel[] tums = new TimUpdateModel[1];
        TimUpdateModel tum = new TimUpdateModel();
        tum.setTimId(1l);
        tum.setMilepostStart(1d);
        tum.setMilepostStop(2d);
        tum.setDirection("both");
        tum.setRoute("I 80");
        tum.setClientId("123");
        tum.setSatRecordId("HEX");
        tum.setActiveTimId(1l);
        tums[0] = tum;

        when(mockRestTemplate.getForEntity("null/active-tim/missing-itis", TimUpdateModel[].class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(tums);
        // Act
        List<ActiveTim> ats = ActiveTimService.getActiveTimsMissingItisCodes();

        // Assert
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
        // Arrange
        TimUpdateModel[] tums = new TimUpdateModel[1];
        TimUpdateModel tum = new TimUpdateModel();
        tum.setTimId(1l);
        tum.setMilepostStart(1d);
        tum.setMilepostStop(2d);
        tum.setDirection("both");
        tum.setRoute("I 80");
        tum.setClientId("123");
        tum.setSatRecordId("HEX");
        tum.setActiveTimId(1l);
        tums[0] = tum;
        when(mockRestTemplate.getForEntity("null/active-tim/not-sent", TimUpdateModel[].class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(tums);

        // Act
        List<ActiveTim> ats = ActiveTimService.getActiveTimsNotSent();

        // Assert
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
    public void deleteActiveTimsById() throws SQLException {
        Mockito.when(mockPreparedStatement.executeUpdate()).thenReturn(2);
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1l);
        activeTimIds.add(-2l);
        boolean success = ActiveTimService.deleteActiveTimsById(activeTimIds);
        assertTrue(success);
        verify(mockConnection).prepareStatement("DELETE FROM ACTIVE_TIM WHERE ACTIVE_TIM_ID in (?,?)");
        verify(mockPreparedStatement).setLong(1, -1l);
        verify(mockPreparedStatement).setLong(2, -2l);
    }

    @Test
    public void getActiveTimsByWydotTim_handleException() throws SQLException {
        // Arrange
        setupWydotTims();
        Mockito.when(DbUtility.getConnectionPool()).thenThrow(sqlException);

        // Act
        List<ActiveTim> aTims = ActiveTimService.getActiveTimsByWydotTim(wydotTims, timTypeId);

        // Assert
        assertTrue(aTims.isEmpty());
        verify(sqlException).printStackTrace();
        PowerMockito.verifyNoMoreInteractions(mockConnection);
        PowerMockito.verifyNoMoreInteractions(mockPreparedStatement);
    }

    @Test
    public void getActiveTimsByWydotTim_success() throws SQLException {
        // Arrange
        setupWydotTims();

        // Act
        List<ActiveTim> aTims = ActiveTimService.getActiveTimsByWydotTim(wydotTims, timTypeId);

        // Assert
        assertFalse(aTims.isEmpty());
        assertEquals(1, aTims.size());
        String sql = "select * from active_tim where TIM_TYPE_ID = ? and (";
        sql += "(CLIENT_ID like ? and DIRECTION = ?)";
        sql += " OR ";
        sql += "(CLIENT_ID like ? and DIRECTION = ?)";
        sql += ")";
        verify(mockConnection).prepareStatement(sql);
        int index = 1;
        verify(mockPreparedStatement).setLong(index, timTypeId);
        index++;
        for (int i = 0; i < wydotTims.size(); i++) {
            verify(mockPreparedStatement).setString(index, wydotTims.get(i).getClientId() + "%");
            index++;
            verify(mockPreparedStatement).setString(index, wydotTims.get(i).getDirection());
            index++;
        }
        verify(mockPreparedStatement).executeQuery();
    }
}