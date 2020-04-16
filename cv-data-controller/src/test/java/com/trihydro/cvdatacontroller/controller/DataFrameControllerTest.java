package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(StrictStubs.class)
public class DataFrameControllerTest extends TestBase<DataFrameController> {

    @Test
    public void GetItisCodesForDataFrameId_Success() throws SQLException {
        // Arrange
        String selectStatement = "select distinct ic.itis_code";
        selectStatement += " from data_frame_itis_Code dfic inner join itis_code ic on dfic.itis_code_id = ic.itis_code_id";
        selectStatement += " where data_frame_id =  -1";

        // Act
        ResponseEntity<String[]> data = uut.GetItisCodesForDataFrameId(-1);

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getString("ITIS_CODE");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        assertEquals(1, data.getBody().length);
    }

    // TODO: test AddDataFrame
}