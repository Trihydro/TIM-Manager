package com.trihydro.cvdatacontroller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DataFrameItisCodeControllerTest extends TestBase<DataFrameItisCodeController> {

    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Spy
    private TimDbTables mockTimDbTables;

    @BeforeEach
    public void setupSubTest() {
        doReturn("").when(mockTimDbTables).buildInsertQueryStatement(any(), any());
        uut.InjectDependencies(mockTimDbTables, mockSqlNullHandler);
    }

    @Test
    public void AddDataFrameItisCode_SUCCESS() throws SQLException {
        // Arrange
        String itis = "9999";
        // Act
        ResponseEntity<Long> data = uut.AddDataFrameItisCode(-1l, itis);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
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
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}