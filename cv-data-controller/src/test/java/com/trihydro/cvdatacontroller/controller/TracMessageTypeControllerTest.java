package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.TracMessageType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class TracMessageTypeControllerTest extends TestBase<TracMessageTypeController> {
    @Test
    public void GetAll_Success() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<TracMessageType>> data = uut.GetAll();

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery("select * from TRAC_MESSAGE_TYPE");
        verify(mockRs).getInt("TRAC_MESSAGE_TYPE_ID");
        verify(mockRs).getString("TRAC_MESSAGE_TYPE");
        verify(mockRs).getString("TRAC_MESSAGE_DESCRIPTION");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, data.getBody().size());
    }
}