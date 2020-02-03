package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataFrameControllerTest extends TestBase<DataFrameController> {

    @Test
    public void GetItisCodesForDataFrameId_Success() throws SQLException {
        // Arrange
        String selectStatement = "select distinct ic.itis_code";
        selectStatement += " from data_frame_itis_Code dfic inner join itis_code ic on dfic.itis_code_id = ic.itis_code_id";
        selectStatement += " where data_frame_id =  -1";

        // Act
        String[] data = uut.GetItisCodesForDataFrameId(-1);

        // Assert
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getString("ITIS_CODE");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        assertEquals(1, data.length);
    }

    // TODO: test AddDataFrame
}