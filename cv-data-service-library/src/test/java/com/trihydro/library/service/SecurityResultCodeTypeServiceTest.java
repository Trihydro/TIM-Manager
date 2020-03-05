package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.trihydro.library.model.SecurityResultCodeType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)

public class SecurityResultCodeTypeServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<SecurityResultCodeType[]> mockResponseEntitySecurityResultCodeTypeArray;

    @Test
    public void getSecurityResultCodeTypes() {
        // Arrange
        SecurityResultCodeType[] srctArr = new SecurityResultCodeType[1];
        SecurityResultCodeType scrt = new SecurityResultCodeType();
        scrt.setSecurityResultCodeType("securityResultCodeType");
        scrt.setSecurityResultCodeTypeId(-1);
        srctArr[0] = scrt;
        doReturn(srctArr).when(mockResponseEntitySecurityResultCodeTypeArray).getBody();
        String url = "null/security-result-code-type/get-all";
        doReturn(mockResponseEntitySecurityResultCodeTypeArray).when(mockRestTemplate).getForEntity(url,
                SecurityResultCodeType[].class);

        // Act
        List<SecurityResultCodeType> data = SecurityResultCodeTypeService.getSecurityResultCodeTypes();

        // Assert
        verify(mockRestTemplate).getForEntity(url, SecurityResultCodeType[].class);
        assertEquals(1, data.size());
    }
}