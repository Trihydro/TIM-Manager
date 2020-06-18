package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.TmddItisCode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(1, response.getBody().size());

        TmddItisCode result = response.getBody().get(0);
        Assertions.assertEquals("AccidentsAndIncidents", result.getElementType());
        Assertions.assertEquals("abandoned vehicle", result.getElementValue());
        Assertions.assertEquals(533, (int) result.getItisCode());
    }

    @Test
    public void getTmddItisCodes_handlesSqlException() throws SQLException {
        // Arrange
        when(mockRs.getString("TMDD_ELEMENT_TYPE")).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<TmddItisCode>> response = uut.selectAllTmddItisCodes();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}