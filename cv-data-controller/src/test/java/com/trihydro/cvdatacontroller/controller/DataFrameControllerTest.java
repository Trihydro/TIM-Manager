package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DataFrameControllerTest extends TestBase<DataFrameController> {

    @Test
    public void GetItisCodesForDataFrameId_Success() throws SQLException {
        // Arrange
        String selectStatement = "select ic.itis_code, dfic.text";
        selectStatement += " from data_frame_itis_code dfic left join itis_code ic on dfic.itis_code_id = ic.itis_code_id";
        selectStatement += " where data_frame_id = -1";
        selectStatement += " order by dfic.position asc";

        // Act
        ResponseEntity<String[]> data = uut.GetItisCodesForDataFrameId(-1);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getString("ITIS_CODE");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        Assertions.assertEquals(1, data.getBody().length);
    }

    @Test
    public void GetItisCodesForDataFrameId_Success_Text() throws SQLException {
        // Arrange
        String selectStatement = "select ic.itis_code, dfic.text";
        selectStatement += " from data_frame_itis_code dfic left join itis_code ic on dfic.itis_code_id = ic.itis_code_id";
        selectStatement += " where data_frame_id = -1";
        selectStatement += " order by dfic.position asc";

        when(mockRs.getString("ITIS_CODE")).thenReturn(null);
        when(mockRs.getString("TEXT")).thenReturn("Closed to light high profile vehicles.");

        // Act
        ResponseEntity<String[]> data = uut.GetItisCodesForDataFrameId(-1);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getString("ITIS_CODE");
        verify(mockRs).getString("TEXT");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        Assertions.assertEquals(1, data.getBody().length);
        Assertions.assertEquals("Closed to light high profile vehicles.", data.getBody()[0]);
    }

    @Test
    public void GetItisCodesForDataFrameId_Fail() throws SQLException {
        // Arrange
        when(mockRs.getString("ITIS_CODE")).thenThrow(new SQLException());

        // Act
        ResponseEntity<String[]> data = uut.GetItisCodesForDataFrameId(-1);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().length);
        verify(mockRs).close();
        verify(mockStatement).close();
        verify(mockConnection).close();
    }

    // TODO: test AddDataFrame
}