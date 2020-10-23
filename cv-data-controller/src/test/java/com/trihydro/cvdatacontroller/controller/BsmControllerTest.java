package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class BsmControllerTest extends TestBase<BsmController> {

    @Test
    public void DeleteOldBsm() throws SQLException {
        // Arrange
        Integer retentionDays = 7;// delete past 7 days old
        Integer maxBsmCoreDataId = 799;
        doReturn(maxBsmCoreDataId).when(mockRs).getInt("maxId");
        String selectStatement = "select max(bsm_core_data_id) maxId from bsm_core_data where record_generated_at";
        selectStatement += " < SYSDATE - INTERVAL '";
        selectStatement += retentionDays + "' DAY";

        // Act
        var data = uut.deleteOldBsm(retentionDays);

        // Assert
        String deleteSQL = "DELETE FROM %s WHERE bsm_core_data_id <= ?";
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertTrue(data.getBody(), "Fail return on success");
        // query
        verify(mockStatement).executeQuery(selectStatement);
        // suve
        verify(mockConnection).prepareStatement(String.format(deleteSQL, "bsm_part2_suve"));
        verify(mockConnection).prepareStatement(String.format(deleteSQL, "bsm_part2_spve"));
        verify(mockConnection).prepareStatement(String.format(deleteSQL, "bsm_part2_vse"));
        verify(mockConnection).prepareStatement(String.format(deleteSQL, "bsm_core_data"));

        verify(mockPreparedStatement, times(4)).setInt(1, maxBsmCoreDataId);
        verify(mockPreparedStatement, times(4)).close();
        verify(mockConnection, times(5)).close();// once for query, and each delete
    }
}