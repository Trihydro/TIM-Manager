package com.trihydro.cvdatacontroller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.cvdatacontroller.controller.ItisCodeController;
import com.trihydro.cvdatacontroller.controller.TestBase;
import com.trihydro.library.model.TmddItisCode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(StrictStubs.class)
public class ItisCodeControllerTest extends TestBase<ItisCodeController> {
    @Test
    public void getTmddItisCodes_success() throws SQLException {
        // Arrange
        when(mockRs.getString("TMDD_ELEMENT_TYPE")).thenReturn("AccidentsAndIncidents");
        when(mockRs.getString("TMDD_ELEMENT_VALUE")).thenReturn("abandoned vehicle");
        when(mockRs.getInt("ITIS_CODE")).thenReturn(533);

        // Act
        ResponseEntity<List<TmddItisCode>> response = uut.selectAllTmddItisCodes();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        TmddItisCode result = response.getBody().get(0);
        assertEquals("AccidentsAndIncidents", result.getElementType());
        assertEquals("abandoned vehicle", result.getElementValue());
        assertEquals(533, (int) result.getItisCode());
    }

    @Test
    public void getTmddItisCodes_handlesSqlException() throws SQLException {
        // Arrange
        when(mockRs.getString("TMDD_ELEMENT_TYPE")).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<TmddItisCode>> response = uut.selectAllTmddItisCodes();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}