package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import com.trihydro.library.helpers.Utility;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.http.HttpStatus;

@RunWith(StrictStubs.class)
public class BsmControllerTest extends TestBase<BsmController> {
    @Mock
    private Utility mockUtility;

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

        assertEquals(HttpStatus.OK, data.getStatusCode());
        assertTrue("Fail return on success", data.getBody());
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