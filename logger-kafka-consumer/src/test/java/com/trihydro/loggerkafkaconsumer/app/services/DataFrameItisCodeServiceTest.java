package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataFrameItisCodeServiceTest
        extends TestBase<com.trihydro.loggerkafkaconsumer.app.services.DataFrameItisCodeService> {

    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    private Long dataFrameId;
    private String itis;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
        dataFrameId = -1l;
        itis = "1234";
    }

    @Test
    public void insertDataFrameItisCode_SUCCESS() throws SQLException {
        // Arrange

        // Act
        Long data = uut.insertDataFrameItisCode(dataFrameId, itis);

        // Assert
        assertEquals(new Long(-1), data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, Long.parseLong(itis));// ITIS_CODE_ID
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 2, dataFrameId);// DATA_FRAME_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3, null);// TEXT
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void insertDataFrameItisCode_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1,
                Long.parseLong(itis));
        // Act
        Long data = uut.insertDataFrameItisCode(dataFrameId, itis);

        // Assert
        assertEquals(new Long(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

}