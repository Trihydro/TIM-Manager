package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.TracMessageType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TracMessageTypeControllerTest extends TestBase<TracMessageTypeController> {
    @Test
    public void GetAll_Success() throws SQLException {
        // Arrange

        // Act
        List<TracMessageType> data = uut.GetAll();

        // Assert
        verify(mockStatement).executeQuery("select * from TRAC_MESSAGE_TYPE");
        verify(mockRs).getInt("trac_message_type_id");
        verify(mockRs).getString("trac_message_type");
        verify(mockRs).getString("trac_message_description");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, data.size());
    }
}