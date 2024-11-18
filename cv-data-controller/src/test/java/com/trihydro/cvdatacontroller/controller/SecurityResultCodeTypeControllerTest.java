package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.SecurityResultCodeType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SecurityResultCodeTypeControllerTest extends TestBase<SecurityResultCodeTypeController> {
    @Test
    public void GetSecurityResultCodeTypes_Success() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<SecurityResultCodeType>> data = uut.GetSecurityResultCodeTypes();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery("select * from SECURITY_RESULT_CODE_TYPE");
        verify(mockRs).getInt("SECURITY_RESULT_CODE_TYPE_ID");
        verify(mockRs).getString("SECURITY_RESULT_CODE_TYPE");
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(1, data.getBody().size());
    }
}