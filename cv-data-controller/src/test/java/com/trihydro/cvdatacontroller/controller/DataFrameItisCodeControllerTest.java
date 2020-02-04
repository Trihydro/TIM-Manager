package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import com.trihydro.cvdatacontroller.helpers.SQLNullHandler;
import com.trihydro.cvdatacontroller.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class DataFrameItisCodeControllerTest extends TestBase<DataFrameItisCodeController> {

    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Spy
    private TimOracleTables mockTimOracleTables;

    @Before
    public void setupSubTest() {
        doReturn("").when(mockTimOracleTables).buildInsertQueryStatement(any(), any());
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    @Test
    public void AddDataFrameItisCode_SUCCESS() throws SQLException {
        // Arrange
        String itis = "9999";
        // Act
        ResponseEntity<Long> data = uut.AddDataFrameItisCode(-1l, itis);

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, Long.parseLong(itis));// ITIS_CODE_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3, null);// TEXT
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 2, -1l);// DATA_FRAME_ID
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void AddDataFrameItisCode_FAIL() throws SQLException {
        // Arrange
        String itis = "9999";
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1,
                Long.parseLong(itis));

        // Act
        ResponseEntity<Long> data = uut.AddDataFrameItisCode(-1l, itis);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}