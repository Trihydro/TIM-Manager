package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

public class DataFrameItisCodeServiceTest
        extends TestBase<com.trihydro.loggerkafkaconsumer.app.services.DataFrameItisCodeService> {

    @Spy
    private TimDbTables mockTimDbTables = new TimDbTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    private Long dataFrameId;
    private String itis;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockTimDbTables, mockSqlNullHandler);
        dataFrameId = -1l;
        itis = "1234";
    }

    @Test
    public void insertDataFrameItisCode_SUCCESS() throws SQLException {
        // Arrange

        // Act
        Long data = uut.insertDataFrameItisCode(dataFrameId, itis, 0);

        // Assert
        Assertions.assertEquals(Long.valueOf(-1), data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, Long.parseLong(itis));// ITIS_CODE_ID
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 2, dataFrameId);// DATA_FRAME_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3, null);// TEXT
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 4, 0);// POSITION
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void insertDataFrameItisCode_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1,
                Long.parseLong(itis));
        // Act
        Long data = uut.insertDataFrameItisCode(dataFrameId, itis, 0);

        // Assert
        Assertions.assertEquals(Long.valueOf(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

}