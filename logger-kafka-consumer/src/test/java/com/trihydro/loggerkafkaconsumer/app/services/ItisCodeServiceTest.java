package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.ItisCode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ItisCodeServiceTest extends TestBase<ItisCodeService> {

    @Test
    public void selectAllItisCodes_SUCCESS() throws SQLException{
        // Arrange

        // Act
        List<ItisCode> data = uut.selectAllItisCodes();

        // Assert
        assertEquals(1, data.size());
        verify(mockStatement).executeQuery("select * from itis_code");
        verify(mockRs).getInt("ITIS_CODE_ID");
        verify(mockRs).getInt("ITIS_CODE");
        verify(mockRs).getString("DESCRIPTION");
        verify(mockRs).getInt("CATEGORY_ID");
        verify(mockStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void selectAllItisCodes_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getInt("ITIS_CODE_ID");
        // Act
        List<ItisCode> data = uut.selectAllItisCodes();

        // Assert
        assertEquals(0, data.size());
        verify(mockStatement).executeQuery("select * from itis_code");
        verify(mockStatement).close();
        verify(mockConnection).close();
    }
}