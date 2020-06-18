package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.cvdatacontroller.tables.TracMessageOracleTables;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.TracMessageSent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class TracMessageSentControllerTest extends TestBase<TracMessageSentController> {

    @Spy
    private TracMessageOracleTables mockTracMessageOracleTables = new TracMessageOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockTracMessageOracleTables, mockSqlNullHandler);
    }

    @Test
    public void selectPacketIds_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<String>> data = uut.SelectPacketIds();

        // Assert
        verify(mockStatement).executeQuery("select PACKET_ID from TRAC_MESSAGE_SENT");
        verify(mockRs).getString("PACKET_ID");
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(1, data.getBody().size());
    }

    @Test
    public void InsertTracMessageSent_SUCCESS() throws SQLException {
        // Arrange
        TracMessageSent mockTMS = Mockito.mock(TracMessageSent.class);
        // Act
        ResponseEntity<Long> key = uut.InsertTracMessageSent(mockTMS);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, key.getStatusCode());
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 1, mockTMS.getTracMessageTypeId());
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 2, mockTMS.getDateTimeSent());
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3, mockTMS.getMessageText());
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 4, mockTMS.getPacketId());
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 5, mockTMS.getRestResponseCode());
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 6, mockTMS.getRestResponseMessage());
        verify(mockSqlNullHandler).setIntegerFromBool(mockPreparedStatement, 7, mockTMS.isMessageSent());
        verify(mockSqlNullHandler).setIntegerFromBool(mockPreparedStatement, 8, mockTMS.isEmailSent());
        Assertions.assertEquals(Long.valueOf(-1), key.getBody());
    }
}