package com.trihydro.cvdatacontroller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.ActiveRsuTimQueryModel;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ActiveTimControllerTest extends TestBase<ActiveTimController> {
    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    private void setupPreparedStatement() {
        doReturn(mockPreparedStatement).when(mockTimOracleTables).buildUpdateStatement(any(), any(), any(), any(),
                any());
    }

    @Test
    public void GetExpiringActiveTims_SUCCESS() throws SQLException {
        // Arrange
        // we only set one property to verify its returned
        when(mockRs.getLong("ACTIVE_TIM_ID")).thenReturn(999l);
        String selectStatement = "SELECT atim.*, tt.type as tim_type_name, tt.description as tim_type_description";
        selectStatement += ", t.msg_cnt, t.url_b, t.is_satellite, t.sat_record_id, t.packet_id";
        selectStatement += ", df.data_frame_id, df.frame_type, df.duration_time, df.ssp_tim_rights, df.ssp_location_rights";
        selectStatement += ", df.ssp_msg_types, df.ssp_msg_content, df.content AS df_Content, df.url";
        selectStatement += ", r.region_id, r.name as region_name, r.anchor_lat, r.anchor_long, r.lane_width";
        selectStatement += ", r.path_id, r.closed_path, r.description AS region_description";
        selectStatement += ", r.directionality, r.direction AS region_direction";
        selectStatement += " FROM active_tim atim";
        selectStatement += " INNER JOIN tim t ON atim.tim_id = t.tim_id";
        selectStatement += " LEFT JOIN data_frame df on atim.tim_id = df.tim_id";
        selectStatement += " LEFT JOIN region r on df.data_frame_id = r.data_frame_id";
        selectStatement += " LEFT JOIN tim_type tt ON atim.tim_type_id = tt.tim_type_id";
        selectStatement += " WHERE atim.tim_start <= SYSDATE + INTERVAL '1' DAY";
        selectStatement += " AND(atim.expiration_date is null OR atim.expiration_date <= SYSDATE + INTERVAL '1' DAY)";
        selectStatement += " AND (atim.tim_end is null OR atim.tim_end >= SYSDATE + INTERVAL '1' DAY)";

        // Act
        ResponseEntity<List<TimUpdateModel>> tums = uut.GetExpiringActiveTims();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, tums.getStatusCode());
        Assertions.assertEquals(1, tums.getBody().size());
        Assertions.assertEquals(Long.valueOf(999), tums.getBody().get(0).getActiveTimId());
        verify(mockStatement).executeQuery(selectStatement);
    }

    @Test
    public void GetExpiringActiveTims_FAIL() throws SQLException {
        // Arrange
        // we only set one property to verify its returned
        when(mockRs.getLong("ACTIVE_TIM_ID")).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<TimUpdateModel>> tums = uut.GetExpiringActiveTims();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, tums.getStatusCode());
        Assertions.assertEquals(0, tums.getBody().size());
        verify(mockRs).close();
        verify(mockStatement).close();
        verify(mockConnection).close();

    }

    @Test
    public void UpdateActiveTim_SatRecordId_FAIL() {
        // Arrange
        setupPreparedStatement();
        doReturn(false).when(mockDbInteractions).updateOrDelete(mockPreparedStatement);

        // Act
        ResponseEntity<Boolean> success = uut.updateActiveTim_SatRecordId(-1l, "asdf");

        // Assert
        Assertions.assertFalse(success.getBody(), "UpdateActiveTim_SatRecordId succeeded when it should have failed");
    }

    @Test
    public void UpdateActiveTim_SatRecordId_SUCCESS() {
        // Arrange
        setupPreparedStatement();
        doReturn(true).when(mockDbInteractions).updateOrDelete(mockPreparedStatement);

        // Act
        ResponseEntity<Boolean> success = uut.updateActiveTim_SatRecordId(-1l, "asdf");

        // Assert
        Assertions.assertTrue(success.getBody(), "UpdateActiveTim_SatRecordId failed when it should have succeeded");
    }

    @Test
    public void UpdateActiveTim_SatRecordId_EXCEPTION() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockDbInteractions).getConnectionPool();

        // Act
        ResponseEntity<Boolean> success = uut.updateActiveTim_SatRecordId(-1l, "asdf");

        // Assert
        Assertions.assertFalse(success.getBody(), "UpdateActiveTim_SatRecordId was successful during an error");
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
        Assertions.assertEquals(HttpStatus.OK, aTims.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getBigDecimal("START_LATITUDE");
        verify(mockRs).getBigDecimal("START_LONGITUDE");
        verify(mockRs).getBigDecimal("END_LATITUDE");
        verify(mockRs).getBigDecimal("END_LONGITUDE");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getString("CLIENT_ID");
        verify(mockRs).getString("SAT_RECORD_ID");
        verify(mockRs).getLong("ACTIVE_TIM_ID");
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(1, aTims.getBody().size());
    }

    @Test
    public void GetActiveTimsMissingItisCodes_FAIL() throws SQLException {
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
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, aTims.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        Assertions.assertEquals(0, aTims.getBody().size());
    }

    @Test
    public void GetActiveTimsNotSent_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select active_tim.* from active_tim";
        statementStr += " left join tim_rsu on active_tim.tim_id = tim_rsu.tim_id";
        statementStr += " where active_tim.sat_record_id is null";
        statementStr += " and tim_rsu.rsu_id is null";

        // Act
        ResponseEntity<List<ActiveTim>> aTims = uut.GetActiveTimsNotSent();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, aTims.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getBigDecimal("START_LATITUDE");
        verify(mockRs).getBigDecimal("START_LONGITUDE");
        verify(mockRs).getBigDecimal("END_LATITUDE");
        verify(mockRs).getBigDecimal("END_LONGITUDE");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getString("CLIENT_ID");
        verify(mockRs).getString("SAT_RECORD_ID");
        verify(mockRs).getLong("ACTIVE_TIM_ID");
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(1, aTims.getBody().size());
    }

    @Test
    public void GetActiveTimsNotSent_FAIL() throws SQLException {
        // Arrange
        String statementStr = "select active_tim.* from active_tim";
        statementStr += " left join tim_rsu on active_tim.tim_id = tim_rsu.tim_id";
        statementStr += " where active_tim.sat_record_id is null";
        statementStr += " and tim_rsu.rsu_id is null";
        when(mockStatement.executeQuery(isA(String.class))).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<ActiveTim>> aTims = uut.GetActiveTimsNotSent();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, aTims.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(0, aTims.getBody().size());
    }

    @Test
    public void GetExpiredActiveTims_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID, ACTIVE_TIM.DIRECTION, SAT_RECORD_ID, START_LATITUDE, START_LONGITUDE, END_LATITUDE, END_LONGITUDE, TYPE, CLIENT_ID, ROUTE, TIM_END, TIM_START, EXPIRATION_DATE, PK, ACTIVE_TIM.TIM_TYPE_ID from active_tim";
        statementStr += " inner join tim_type on tim_type.tim_type_id = active_tim.tim_type_id";
        statementStr += "  WHERE TIM_END <= SYS_EXTRACT_UTC(SYSTIMESTAMP)";

        // Act
        ResponseEntity<List<ActiveTim>> aTims = uut.GetExpiredActiveTims();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, aTims.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getLong("ACTIVE_TIM_ID");
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getString("SAT_RECORD_ID");
        verify(mockRs).getBigDecimal("START_LATITUDE");
        verify(mockRs).getBigDecimal("START_LONGITUDE");
        verify(mockRs).getBigDecimal("END_LATITUDE");
        verify(mockRs).getBigDecimal("END_LONGITUDE");
        verify(mockRs).getString("TYPE");
        verify(mockRs).getString("CLIENT_ID");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getString("DIRECTION");
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(1, aTims.getBody().size());
    }

    @Test
    public void GetExpiredActiveTims_FAIL() throws SQLException {
        // Arrange
        String statementStr = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID, ACTIVE_TIM.DIRECTION, SAT_RECORD_ID, START_LATITUDE, START_LONGITUDE, END_LATITUDE, END_LONGITUDE, TYPE, CLIENT_ID, ROUTE, TIM_END, TIM_START, EXPIRATION_DATE, PK, ACTIVE_TIM.TIM_TYPE_ID from active_tim";
        statementStr += " inner join tim_type on tim_type.tim_type_id = active_tim.tim_type_id";
        statementStr += "  WHERE TIM_END <= SYS_EXTRACT_UTC(SYSTIMESTAMP)";
        when(mockStatement.executeQuery(isA(String.class))).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<ActiveTim>> aTims = uut.GetExpiredActiveTims();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, aTims.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(0, aTims.getBody().size());
    }

    @Test
    public void GetActiveTimIndicesByRsu_SUCCESS() throws SQLException {
        // Arrange
        String rsuTarget = "10.0.0.1";
        String selectStatement = "select tim_rsu.rsu_index from active_tim";
        selectStatement += " inner join tim on active_tim.tim_id = tim.tim_id";
        selectStatement += " inner join tim_rsu on tim_rsu.tim_id = tim.tim_id";
        selectStatement += " inner join rsu on rsu.rsu_id = tim_rsu.rsu_id";
        selectStatement += " inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
        selectStatement += " where rsu_vw.ipv4_address = '" + rsuTarget + "'";

        // Act
        ResponseEntity<List<Integer>> data = uut.GetActiveTimIndicesByRsu(rsuTarget);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getInt("RSU_INDEX");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetActiveTimIndicesByRsu_FAIL() throws SQLException {
        // Arrange
        String rsuTarget = "10.0.0.1";
        String selectStatement = "select tim_rsu.rsu_index from active_tim";
        selectStatement += " inner join tim on active_tim.tim_id = tim.tim_id";
        selectStatement += " inner join tim_rsu on tim_rsu.tim_id = tim.tim_id";
        selectStatement += " inner join rsu on rsu.rsu_id = tim_rsu.rsu_id";
        selectStatement += " inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
        selectStatement += " where rsu_vw.ipv4_address = '" + rsuTarget + "'";
        doThrow(new SQLException()).when(mockRs).getInt("RSU_INDEX");

        // Act
        ResponseEntity<List<Integer>> data = uut.GetActiveTimIndicesByRsu(rsuTarget);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetActiveTimsByClientIdDirection_SUCCESS() throws SQLException {
        // Arrange
        String clientId = "clientId";
        Long timTypeId = -1l;
        String direction = "eastward";
        String selectStatement = "select * from active_tim where CLIENT_ID = '" + clientId + "' and TIM_TYPE_ID = "
                + timTypeId;
        selectStatement += " and DIRECTION = '" + direction + "'";

        // Act
        ResponseEntity<List<ActiveTim>> data = uut.GetActiveTimsByClientIdDirection(clientId, timTypeId, direction);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getLong("ACTIVE_TIM_ID");
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getString("SAT_RECORD_ID");
        verify(mockRs).getString("CLIENT_ID");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getString("TIM_END");
        verify(mockRs).getString("TIM_START");
        verify(mockRs).getBigDecimal("START_LATITUDE");
        verify(mockRs).getBigDecimal("START_LONGITUDE");
        verify(mockRs).getBigDecimal("END_LATITUDE");
        verify(mockRs).getBigDecimal("END_LONGITUDE");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getInt("PK");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetActiveTimsByClientIdDirection_FAIL() throws SQLException {
        // Arrange
        String clientId = "clientId";
        Long timTypeId = -1l;
        String direction = "eastward";
        String selectStatement = "select * from active_tim where CLIENT_ID = '" + clientId + "' and TIM_TYPE_ID = "
                + timTypeId;
        selectStatement += " and DIRECTION = '" + direction + "'";
        doThrow(new SQLException()).when(mockRs).getLong("ACTIVE_TIM_ID");

        // Act
        ResponseEntity<List<ActiveTim>> data = uut.GetActiveTimsByClientIdDirection(clientId, timTypeId, direction);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetItisCodesForActiveTim_SUCCESS() throws SQLException {
        // Arrange
        Long activeTimId = -1l;
        String selectStatement = "select itis_code from active_tim ";
        selectStatement += "inner join tim on tim.tim_id = active_tim.tim_id ";
        selectStatement += "inner join data_frame on tim.tim_id = data_frame.tim_id ";
        selectStatement += "inner join data_frame_itis_code on data_frame_itis_code.data_frame_id = data_frame.data_frame_id ";
        selectStatement += "inner join itis_code on data_frame_itis_code.itis_code_id = itis_code.itis_code_id ";
        selectStatement += "where active_tim_id = " + activeTimId;

        // Act
        ResponseEntity<List<Integer>> data = uut.GetItisCodesForActiveTim(activeTimId);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getInt("ITIS_CODE");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetItisCodesForActiveTim_FAIL() throws SQLException {
        // Arrange
        Long activeTimId = -1l;
        String selectStatement = "select itis_code from active_tim inner join tim on tim.tim_id = active_tim.tim_id inner join data_frame on tim.tim_id = data_frame.tim_id inner join data_frame_itis_code on data_frame_itis_code.data_frame_id = data_frame.data_frame_id inner join itis_code on data_frame_itis_code.itis_code_id = itis_code.itis_code_id where active_tim_id = "
                + activeTimId;
        doThrow(new SQLException()).when(mockRs).getInt("ITIS_CODE");

        // Act
        ResponseEntity<List<Integer>> data = uut.GetItisCodesForActiveTim(activeTimId);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void DeleteActiveTim_SUCCESS() throws SQLException {
        // Arrange
        Long activeTimId = -1l;
        // Act
        ResponseEntity<Boolean> data = uut.DeleteActiveTim(activeTimId);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertTrue(data.getBody(), "Fail return on success");
        verify(mockConnection).prepareStatement("DELETE FROM ACTIVE_TIM WHERE ACTIVE_TIM_ID = ?");
        verify(mockPreparedStatement).setLong(1, activeTimId);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void DeleteActiveTim_FAIL() throws SQLException {
        // Arrange
        Long activeTimId = -1l;
        doThrow(new SQLException()).when(mockPreparedStatement).setLong(1, activeTimId);

        // Act
        ResponseEntity<Boolean> data = uut.DeleteActiveTim(activeTimId);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertFalse(data.getBody(), "Success returned on error");
        verify(mockConnection).prepareStatement("DELETE FROM ACTIVE_TIM WHERE ACTIVE_TIM_ID = ?");
        verify(mockPreparedStatement).setLong(1, activeTimId);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void DeleteActiveTimsById_SUCCESS() throws SQLException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(Long.valueOf(-1));

        // Act
        ResponseEntity<Boolean> data = uut.DeleteActiveTimsById(activeTimIds);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertTrue(data.getBody(), "Fail return on success");
        verify(mockConnection).prepareStatement("DELETE FROM ACTIVE_TIM WHERE ACTIVE_TIM_ID in (?)");
        verify(mockPreparedStatement).setLong(1, -1l);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void DeleteActiveTimsById_FAIL() throws SQLException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(Long.valueOf(-1));
        doThrow(new SQLException()).when(mockPreparedStatement).setLong(1, -1l);

        // Act
        ResponseEntity<Boolean> data = uut.DeleteActiveTimsById(activeTimIds);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertFalse(data.getBody(), "Success return on error");
        verify(mockConnection).prepareStatement("DELETE FROM ACTIVE_TIM WHERE ACTIVE_TIM_ID in (?)");
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void GetActiveTimsByIds_SUCCESS() throws SQLException {
        // Arrange
        String query = "select * from active_tim where active_tim_id in (?, ?)";

        // Act
        var response = uut.GetActiveTimsByIds(Arrays.asList(-1l, -2l));

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mockConnection).prepareStatement(query);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void GetActiveTimsByIds_FAIL() throws SQLException {
        // Arrange
        String query = "select * from active_tim where active_tim_id in (?, ?)";
        doThrow(new SQLException()).when(mockPreparedStatement).setLong(1, -1l);

        // Act
        var response = uut.GetActiveTimsByIds(Arrays.asList(-1l, -2l));

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(mockConnection).prepareStatement(query);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void GetActiveTimsByIds_BadRequest() throws SQLException {
        // Arrange

        // Act
        var response = uut.GetActiveTimsByIds(null);

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void GetActiveTimsByWydotTim_SUCCESS() throws SQLException {
        // Arrange
        List<WydotTim> wydotTims = new ArrayList<>();
        WydotTim wydotTim = new WydotTim();
        wydotTim.setClientId("clientId");
        wydotTim.setDirection("westward");
        wydotTims.add(wydotTim);
        Long timTypeId = -1l;
        String query = "select * from active_tim where TIM_TYPE_ID = ? and ((CLIENT_ID like ? and DIRECTION = ?))";

        // Act
        ResponseEntity<List<ActiveTim>> data = uut.GetActiveTimsByWydotTim(wydotTims, timTypeId);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().size());
        verify(mockConnection).prepareStatement(query);
        verify(mockPreparedStatement).setLong(1, timTypeId);
        verify(mockPreparedStatement).setString(2, wydotTim.getClientId() + "%");
        verify(mockPreparedStatement).setString(3, wydotTim.getDirection());
        verify(mockRs).getLong("ACTIVE_TIM_ID");
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getString("SAT_RECORD_ID");
        verify(mockRs).getString("CLIENT_ID");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getString("TIM_END");
        verify(mockRs).getString("TIM_START");
        verify(mockRs).getBigDecimal("START_LATITUDE");
        verify(mockRs).getBigDecimal("START_LONGITUDE");
        verify(mockRs).getBigDecimal("END_LATITUDE");
        verify(mockRs).getBigDecimal("END_LONGITUDE");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getInt("PK");
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void GetActiveTimsByWydotTim_FAIL() throws SQLException {
        // Arrange
        List<WydotTim> wydotTims = new ArrayList<>();
        WydotTim wydotTim = new WydotTim();
        wydotTim.setClientId("clientId");
        wydotTim.setDirection("westward");
        wydotTims.add(wydotTim);
        Long timTypeId = -1l;
        String query = "select * from active_tim where TIM_TYPE_ID = ? and ((CLIENT_ID like ? and DIRECTION = ?))";
        doThrow(new SQLException()).when(mockPreparedStatement).setLong(1, timTypeId);

        // Act
        ResponseEntity<List<ActiveTim>> data = uut.GetActiveTimsByWydotTim(wydotTims, timTypeId);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().size());
        verify(mockConnection).prepareStatement(query);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void GetActivesTimByType_SUCCESS() throws SQLException {
        // Arrange
        Long timTypeId = -1l;
        String query = "select * from active_tim where TIM_TYPE_ID = " + timTypeId;

        // Act
        ResponseEntity<List<ActiveTim>> data = uut.GetActiveTimsByType(timTypeId);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().size());
        verify(mockStatement).executeQuery(query);
        verify(mockRs).getLong("ACTIVE_TIM_ID");
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getString("SAT_RECORD_ID");
        verify(mockRs).getString("CLIENT_ID");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getString("TIM_END");
        verify(mockRs).getString("TIM_START");
        verify(mockRs).getBigDecimal("START_LATITUDE");
        verify(mockRs).getBigDecimal("START_LONGITUDE");
        verify(mockRs).getBigDecimal("END_LATITUDE");
        verify(mockRs).getBigDecimal("END_LONGITUDE");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getInt("PK");
        verify(mockRs).getLong("TIM_TYPE_ID");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetActivesTimByType_FAIL() throws SQLException {
        // Arrange
        Long timTypeId = -1l;
        String query = "select * from active_tim where TIM_TYPE_ID = " + timTypeId;
        doThrow(new SQLException()).when(mockRs).getLong("ACTIVE_TIM_ID");

        // Act
        ResponseEntity<List<ActiveTim>> data = uut.GetActiveTimsByType(timTypeId);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().size());
        verify(mockStatement).executeQuery(query);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetAllActiveTims_SUCCESS() throws SQLException {
        // Arrange
        String query = "select * from active_tim";

        // Act
        ResponseEntity<List<ActiveTim>> data = uut.GetAllActiveTims();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().size());
        verify(mockStatement).executeQuery(query);
        verify(mockRs).getLong("ACTIVE_TIM_ID");
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getString("SAT_RECORD_ID");
        verify(mockRs).getBigDecimal("START_LATITUDE");
        verify(mockRs).getBigDecimal("START_LONGITUDE");
        verify(mockRs).getBigDecimal("END_LATITUDE");
        verify(mockRs).getBigDecimal("END_LONGITUDE");
        verify(mockRs).getString("CLIENT_ID");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetAllActiveTims_FAIL() throws SQLException {
        // Arrange
        String query = "select * from active_tim";
        doThrow(new SQLException()).when(mockRs).getLong("ACTIVE_TIM_ID");

        // Act
        ResponseEntity<List<ActiveTim>> data = uut.GetAllActiveTims();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().size());
        verify(mockStatement).executeQuery(query);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetActiveRsuTims_SUCCESS() throws SQLException {
        // Act
        ResponseEntity<List<ActiveTim>> data = uut.GetActiveRsuTims();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockRs).getLong("ACTIVE_TIM_ID");
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getString("TIM_START");
        verify(mockRs).getString("TIM_END");
        verify(mockRs).getLong("TIM_TYPE_ID");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getString("CLIENT_ID");
        verify(mockRs).getInt("PK");
        verify(mockRs).getString("IPV4_ADDRESS");
        verify(mockRs).getInt("RSU_INDEX");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetActiveRsuTims_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockStatement).executeQuery(any());

        // Act
        ResponseEntity<List<ActiveTim>> data = uut.GetActiveRsuTims();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertNull(data.getBody());
        verify(mockStatement).executeQuery(any());
        verify(mockStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void GetActiveRsuTim_SUCCESS() throws SQLException {
        // Arrange
        String clientId = "clientid";
        String direction = "eastward";
        String ipv4Address = "10.0.0.1";
        String query = "select * from active_tim";
        query += " inner join tim_rsu on active_tim.tim_id = tim_rsu.tim_id";
        query += " inner join rsu on tim_rsu.rsu_id = rsu.rsu_id";
        query += " inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
        query += " where ipv4_address = '" + ipv4Address + "' and client_id = '" + clientId;
        query += "' and active_tim.direction = '" + direction + "'";

        // Act
        ActiveRsuTimQueryModel artqm = new ActiveRsuTimQueryModel(direction, clientId, ipv4Address);
        ResponseEntity<ActiveTim> data = uut.GetActiveRsuTim(artqm);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertNotNull(data.getBody(), "ActiveTim should not be null");
        verify(mockStatement).executeQuery(query);
        verify(mockRs).getLong("ACTIVE_TIM_ID");
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getString("SAT_RECORD_ID");
        verify(mockRs).getString("CLIENT_ID");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getString("TIM_END");
        verify(mockRs).getString("TIM_START");
        verify(mockRs).getBigDecimal("START_LATITUDE");
        verify(mockRs).getBigDecimal("START_LONGITUDE");
        verify(mockRs).getBigDecimal("END_LATITUDE");
        verify(mockRs).getBigDecimal("END_LONGITUDE");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getInt("PK");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetActiveRsuTim_FAIL() throws SQLException {
        // Arrange
        String clientId = "clientid";
        String direction = "eastward";
        String ipv4Address = "10.0.0.1";
        String query = "select * from active_tim";
        query += " inner join tim_rsu on active_tim.tim_id = tim_rsu.tim_id";
        query += " inner join rsu on tim_rsu.rsu_id = rsu.rsu_id";
        query += " inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
        query += " where ipv4_address = '" + ipv4Address + "' and client_id = '" + clientId;
        query += "' and active_tim.direction = '" + direction + "'";
        doThrow(new SQLException()).when(mockStatement).executeQuery(query);

        // Act
        ActiveRsuTimQueryModel artqm = new ActiveRsuTimQueryModel(direction, clientId, ipv4Address);
        ResponseEntity<ActiveTim> data = uut.GetActiveRsuTim(artqm);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertNull(data.getBody(), "ActiveTim should be null");
        verify(mockStatement).executeQuery(query);
        verify(mockStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void UpdateExpiration_SUCCESS() throws SQLException {
        // Arrange
        // note that expirationDate comes from the topic.OdeTIMCertExpirationTimeJson in
        // Iso8601 format
        // so that's what we'll use here
        var packetID = "3C8E8DF2470B1A772E";
        var startDate = "2020-10-14T15:37:26.037Z";
        var expDate = "2020-10-20T16:26:07.000Z";
        doReturn(mockPreparedStatement).when(mockConnection).prepareStatement(any());
        doReturn(true).when(mockDbInteractions).updateOrDelete(mockPreparedStatement);

        String updateStatement = "UPDATE ACTIVE_TIM SET EXPIRATION_DATE = ? WHERE ACTIVE_TIM_ID IN (";
        updateStatement += "SELECT ACTIVE_TIM_ID FROM ACTIVE_TIM atim";
        updateStatement += " INNER JOIN TIM ON atim.TIM_ID = TIM.TIM_ID";
        updateStatement += " WHERE TIM.PACKET_ID = '?' AND atim.TIM_START = '?'";
        updateStatement += ")";

        // Act
        ResponseEntity<Boolean> success = uut.UpdateExpiration(packetID, startDate, expDate);

        // Assert
        Assertions.assertTrue(success.getBody(), "UpdateExpiration failed when it should have succeeded");
        verify(mockConnection).prepareStatement(updateStatement);
        verify(mockPreparedStatement).setObject(1, "20-Oct-20 09.26.07.000 AM");
        verify(mockPreparedStatement).setObject(2, packetID);
        verify(mockPreparedStatement).setObject(3, "14-Oct-20 08.37.26.037 AM");
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void UpdateExpiration_FAIL() throws SQLException {
        // Arrange
        // note that expirationDate comes from the topic.OdeTIMCertExpirationTimeJson in
        // Iso8601 format
        // so that's what we'll use here
        var packetID = "3C8E8DF2470B1A772E";
        var startDate = "2020-10-14T15:37:26.037Z";
        var expDate = "2020-10-20T16:26:07.000Z";
        doReturn(mockPreparedStatement).when(mockConnection).prepareStatement(any());
        doReturn(false).when(mockDbInteractions).updateOrDelete(mockPreparedStatement);

        String updateStatement = "UPDATE ACTIVE_TIM SET EXPIRATION_DATE = ? WHERE ACTIVE_TIM_ID IN (";
        updateStatement += "SELECT ACTIVE_TIM_ID FROM ACTIVE_TIM atim";
        updateStatement += " INNER JOIN TIM ON atim.TIM_ID = TIM.TIM_ID";
        updateStatement += " WHERE TIM.PACKET_ID = '?' AND atim.TIM_START = '?'";
        updateStatement += ")";

        // Act
        ResponseEntity<Boolean> success = uut.UpdateExpiration(packetID, startDate, expDate);

        // Assert
        Assertions.assertFalse(success.getBody(), "UpdateExpiration succeeded when it should have failed");
        verify(mockConnection).prepareStatement(updateStatement);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void UpdateExpiration_EXCEPTION() throws SQLException {
        // Arrange
        // note that expirationDate comes from the topic.OdeTIMCertExpirationTimeJson in
        // Iso8601 format
        // so that's what we'll use here
        var packetID = "3C8E8DF2470B1A772E";
        var startDate = "2020-10-14T15:37:26.037Z";
        var expDate = "2020-10-20T16:26:07.000Z";
        doThrow(new SQLException()).when(mockDbInteractions).getConnectionPool();

        // Act
        ResponseEntity<Boolean> success = uut.UpdateExpiration(packetID, startDate, expDate);

        // Assert
        Assertions.assertFalse(success.getBody(), "UpdateExpiration succeeded when it should have thrown an error");
    }

    @Test
    public void InsertActiveTim_SUCCESS() throws SQLException {
        // Arrange
        ActiveTim activeTim = new ActiveTim();
        activeTim.setStartPoint(new Coordinate(BigDecimal.valueOf(-1), BigDecimal.valueOf(-2)));
        activeTim.setEndPoint(new Coordinate(BigDecimal.valueOf(-3), BigDecimal.valueOf(-4)));
        String startTime = Instant.now().toString();
        String endTime = Instant.now().plusSeconds(60).toString();
        activeTim.setStartDateTime(startTime);
        activeTim.setEndDateTime(endTime);

        // Act
        ResponseEntity<Long> data = uut.InsertActiveTim(activeTim);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, activeTim.getTimId());// TIM_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, activeTim.getDirection());// DIRECTION
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 3, java.sql.Timestamp
                .valueOf(LocalDateTime.parse(activeTim.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME)));// TIM_START
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 4, java.sql.Timestamp
                .valueOf(LocalDateTime.parse(activeTim.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME)));// TIM_END
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 5, activeTim.getTimTypeId());// TIM_TYPE_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 6, activeTim.getRoute());// ROUTE
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 7, activeTim.getClientId());// CLIENT_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 8, activeTim.getSatRecordId());// SAT_RECORD_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 9, activeTim.getPk());// PK
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 10,
                activeTim.getStartPoint().getLatitude());// START_LATITUDE
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 11,
                activeTim.getStartPoint().getLongitude());// START_LONGITUDE
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 12,
                activeTim.getEndPoint().getLatitude());// END_LATITUDE
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 13,
                activeTim.getEndPoint().getLongitude());// END_LONGITUDE
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();

    }

    @Test
    public void InsertActiveTim_FAIL() throws SQLException {
        // Arrange
        ActiveTim activeTim = new ActiveTim();
        String startTime = Instant.now().toString();
        String endTime = Instant.now().plusSeconds(60).toString();
        activeTim.setStartDateTime(startTime);
        activeTim.setEndDateTime(endTime);
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1,
                activeTim.getTimId());

        // Act
        ResponseEntity<Long> data = uut.InsertActiveTim(activeTim);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();

    }

}