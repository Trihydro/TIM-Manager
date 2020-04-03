package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.Milepost;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class MilepostControllerTest extends TestBase<MilepostController> {
    @Test
    public void getMileposts_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<Milepost>> data = uut.getMileposts();

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement)
                .executeQuery("select * from MILEPOST_VW_NEW where MOD(milepost, 1) = 0 order by milepost asc");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, data.getBody().size());
    }

    @Test
    public void getMileposts_FAIL() throws SQLException {
        // Arrange
        when(mockRs.getString(isA(String.class))).thenThrow(new SQLException());
        // Act
        ResponseEntity<List<Milepost>> data = uut.getMileposts();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement)
                .executeQuery("select * from MILEPOST_VW_NEW where MOD(milepost, 1) = 0 order by milepost asc");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(0, data.getBody().size());
    }

    @Test
    public void getRoutes_SUCCESS() throws SQLException {
        // Arrange
        doReturn("common name").when(mockRs).getString("COMMON_NAME");

        // Act
        ResponseEntity<List<String>> data = uut.getRoutes();

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        assertEquals(1, data.getBody().size());
    }

    @Test
    public void getRoutes_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getString("COMMON_NAME");

        // Act
        ResponseEntity<List<String>> data = uut.getRoutes();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        assertEquals(0, data.getBody().size());
    }

}