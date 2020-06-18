package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.IncidentChoice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class IncidentChoiceControllerTest extends TestBase<IncidentChoiceController> {

    @Test
    public void SelectAllIncidentActions_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<IncidentChoice>> data = uut.SelectAllIncidentActions();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().size());
        verify(mockStatement).executeQuery("select * from incident_action_lut");
        verify(mockRs).getInt("itis_code_id");
        verify(mockRs).getString("description");
        verify(mockRs).getString("code");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void SelectAllIncidentActions_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getInt("itis_code_id");

        // Act
        ResponseEntity<List<IncidentChoice>> data = uut.SelectAllIncidentActions();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().size());
        verify(mockStatement).executeQuery("select * from incident_action_lut");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void SelectAllIncidentEffects_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<IncidentChoice>> data = uut.SelectAllIncidentEffects();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().size());
        verify(mockStatement).executeQuery("select * from incident_effect_lut");
        verify(mockRs).getInt("itis_code_id");
        verify(mockRs).getString("description");
        verify(mockRs).getString("code");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void SelectAllIncidentEffects_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getInt("itis_code_id");

        // Act
        ResponseEntity<List<IncidentChoice>> data = uut.SelectAllIncidentEffects();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().size());
        verify(mockStatement).executeQuery("select * from incident_effect_lut");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void SelectAllIncidentProblems_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<IncidentChoice>> data = uut.SelectAllIncidentProblems();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().size());
        verify(mockStatement).executeQuery("select * from incident_problem_lut");
        verify(mockRs).getInt("itis_code_id");
        verify(mockRs).getString("description");
        verify(mockRs).getString("code");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void SelectAllIncidentProblems_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getInt("itis_code_id");

        // Act
        ResponseEntity<List<IncidentChoice>> data = uut.SelectAllIncidentProblems();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().size());
        verify(mockStatement).executeQuery("select * from incident_problem_lut");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }
}