package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.trihydro.cvdatacontroller.tables.TimOracleTables;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimUpdateModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    private ActiveTimController uut;

    // @Rule
    // public final ExpectedException exception = ExpectedException.none();

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
    public void GetExpiringActiveTims() throws SQLException {
        // Arrange
        // we only set one property to verify its returned
        when(mockRs.getLong("ACTIVE_TIM_ID")).thenReturn(999l);

        // Act
        List<TimUpdateModel> tums = uut.GetExpiringActiveTims();

        // Assert
        assertEquals(1, tums.size());
        assertEquals(new Long(999), tums.get(0).getActiveTimId());
    }

    @Test
    public void UpdateActiveTim_SatRecordId_FAIL() {
        // Arrange
        doReturn(false).when(uut).updateOrDelete(mockPreparedStatement);

        // Act
        boolean success = uut.updateActiveTim_SatRecordId(-1l, "asdf");

        // Assert
        assertFalse("UpdateActiveTim_SatRecordId succeeded when it should have failed", success);
    }

    @Test
    public void UpdateActiveTim_SatRecordId_SUCCESS() {
        // Arrange
        doReturn(true).when(uut).updateOrDelete(mockPreparedStatement);

        // Act
        boolean success = uut.updateActiveTim_SatRecordId(-1l, "asdf");

        // Assert
        assertTrue("UpdateActiveTim_SatRecordId failed when it should have succeeded", success);
    }

    @Test
    public void GetActiveTimsMissingItisCodes_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = " select * from active_tim where active_tim.tim_id in";
        statementStr += " (select active_tim.tim_id from active_tim";
        statementStr += " left join data_frame on active_tim.tim_id = data_frame.tim_id";
        statementStr += " left join data_frame_itis_code on data_frame.data_frame_id = data_frame_itis_code.data_frame_id";
        statementStr += " where active_tim.tim_id in";
        statementStr += " (select active_tim.tim_id from active_tim";
        statementStr += " left join data_frame on active_tim.tim_id = data_frame.tim_id";
        statementStr += " left join data_frame_itis_code ON data_frame.data_frame_id = data_frame_itis_code.data_frame_id";
        statementStr += " where data_frame_itis_code.itis_code_id is null)";
        statementStr += " group by active_tim.tim_id";
        statementStr += " having max(data_frame_itis_code.itis_code_id) is null)";

        // Act
        ResponseEntity<List<ActiveTim>> aTims = uut.GetActiveTimsMissingItisCodes();

        // Assert
        assertEquals(HttpStatus.OK, aTims.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getDouble("MILEPOST_START");
        verify(mockRs).getDouble("MILEPOST_STOP");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getString("CLIENT_ID");
        verify(mockRs).getString("SAT_RECORD_ID");
        verify(mockRs).getLong("ACTIVE_TIM_ID");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, aTims.getBody().size());
    }

    @Test
    public void GetActiveTimsMissingItisCodes_FAIL() throws SQLException{
        // Arrange
        String statementStr = " select * from active_tim where active_tim.tim_id in";
        statementStr += " (select active_tim.tim_id from active_tim";
        statementStr += " left join data_frame on active_tim.tim_id = data_frame.tim_id";
        statementStr += " left join data_frame_itis_code on data_frame.data_frame_id = data_frame_itis_code.data_frame_id";
        statementStr += " where active_tim.tim_id in";
        statementStr += " (select active_tim.tim_id from active_tim";
        statementStr += " left join data_frame on active_tim.tim_id = data_frame.tim_id";
        statementStr += " left join data_frame_itis_code ON data_frame.data_frame_id = data_frame_itis_code.data_frame_id";
        statementStr += " where data_frame_itis_code.itis_code_id is null)";
        statementStr += " group by active_tim.tim_id";
        statementStr += " having max(data_frame_itis_code.itis_code_id) is null)";
        when(mockRs.getLong(isA(String.class))).thenThrow(new SQLException("error"));

        // Act
        ResponseEntity<List<ActiveTim>> aTims = uut.GetActiveTimsMissingItisCodes();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, aTims.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        assertEquals(0, aTims.getBody().size());
    }

    @Test
    public void GetActiveTimsNotSent_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select active_tim.* from active_tim";
        statementStr += " left join tim_rsu on active_tim.tim_id = tim_rsu.tim_id";
        statementStr += " where active_tim.sat_record_id is null";
        statementStr += " and tim_rsu.rsu_id is null";

        // Act
        List<ActiveTim> aTims = uut.GetActiveTimsNotSent();

        // Assert
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getDouble("MILEPOST_START");
        verify(mockRs).getDouble("MILEPOST_STOP");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getString("CLIENT_ID");
        verify(mockRs).getString("SAT_RECORD_ID");
        verify(mockRs).getLong("ACTIVE_TIM_ID");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, aTims.size());
    }

    @Test
    public void GetExpiredActiveTims_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID, ACTIVE_TIM.DIRECTION, SAT_RECORD_ID, MILEPOST_START, MILEPOST_STOP, TYPE, CLIENT_ID, ROUTE from active_tim";
        statementStr += " inner join tim_type on tim_type.tim_type_id = active_tim.tim_type_id";
        statementStr += "  WHERE TIM_END <= SYS_EXTRACT_UTC(SYSTIMESTAMP)";

        // Act
        List<ActiveTim> aTims = uut.GetExpiredActiveTims();

        // Assert
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getLong("ACTIVE_TIM_ID");
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getString("SAT_RECORD_ID");
        verify(mockRs).getDouble("MILEPOST_START");
        verify(mockRs).getDouble("MILEPOST_STOP");
        verify(mockRs).getString("TYPE");
        verify(mockRs).getString("CLIENT_ID");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getString("DIRECTION");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, aTims.size());
    }
}