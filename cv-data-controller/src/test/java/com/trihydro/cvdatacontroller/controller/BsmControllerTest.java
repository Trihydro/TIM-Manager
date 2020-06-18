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
        String strDate = uut.getOneMonthPrior();
        doReturn(strDate).when(uut).getOneMonthPrior();

        // Act
        var data = uut.deleteOldBsm();

        // Assert
        String deleteSQL = "DELETE FROM %s WHERE bsm_core_data_id IN";
        deleteSQL += " (SELECT bsm_core_data_id FROM bsm_core_data WHERE ode_received_at < ?)";

        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertTrue(data.getBody(), "Fail return on success");
        // suve
        verify(mockConnection).prepareStatement(String.format(deleteSQL, "bsm_part2_suve"));
        verify(mockConnection).prepareStatement(String.format(deleteSQL, "bsm_part2_spve"));
        verify(mockConnection).prepareStatement(String.format(deleteSQL, "bsm_part2_vse"));
        verify(mockConnection).prepareStatement("DELETE FROM bsm_core_data WHERE ode_received_at < ?");

        verify(mockPreparedStatement, times(4)).setString(1, strDate);
        verify(mockPreparedStatement, times(4)).close();
        verify(mockConnection, times(4)).close();
    }
}