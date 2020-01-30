package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TracMessageSentControllerTest extends TestBase<TracMessageSentController> {
    @Test
    public void selectPacketIds_SUCCESS() throws SQLException {
        // Arrange

        // Act
        List<String> data = uut.selectPacketIds();

        // Assert
        verify(mockStatement).executeQuery("select PACKET_ID from TRAC_MESSAGE_SENT");
        verify(mockRs).getString("packet_id");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, data.size());
    }
}