package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DataFrameControllerTest extends TestBase<DataFrameController> {

    @Test
    public void GetItisCodesForDataFrameId_Success() throws SQLException {
        // Arrange
        String selectStatement = "select ic.itis_code";
        selectStatement += " from data_frame_itis_code dfic inner join itis_code ic on dfic.itis_code_id = ic.itis_code_id";
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

    // TODO: test AddDataFrame
}