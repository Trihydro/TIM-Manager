package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.cvdatacontroller.tables.TimOracleTables;
import com.trihydro.library.model.TimUpdateModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActiveTimControllerTest extends TestBase<ActiveTimController> {
    @Mock
    private TimOracleTables mockTimOracleTables;   
    
    @Before
    public void setupSubTest(){
        uut.SetTables(mockTimOracleTables);
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