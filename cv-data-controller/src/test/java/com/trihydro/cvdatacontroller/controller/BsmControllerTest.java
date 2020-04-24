package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.http.HttpStatus;

@RunWith(StrictStubs.class)
public class BsmControllerTest extends TestBase<BsmController> {
    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Mock
    private Utility mockUtility;

    @Test
    public void DeleteOldBsm() throws SQLException {
        // Arrange
        DateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
        TimeZone toTimeZone = TimeZone.getTimeZone("MST");
        sdf.setTimeZone(toTimeZone);
        Date dte = java.sql.Date.valueOf(LocalDate.now().minus(1, ChronoUnit.MONTHS));
        String strDate = sdf.format(dte.getTime());
        doReturn(strDate).when(uut).getOneMonthPrior();

        // Act
        var data = uut.DeleteOldBsm();

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