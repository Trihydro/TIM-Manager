package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.TimType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

public class TimTypeServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<TimType[]> mockResponseEntityTimTypeArray;
    @Mock
    protected CVRestServiceProps mockCVRestServiceProps;

    @InjectMocks
    private TimTypeService uut;

    private String baseUrl = "baseUrl";

    @Test
    public void selectAll() {
        // Arrange
        doReturn(baseUrl).when(mockCVRestServiceProps).getCvRestService();

        String url = String.format("%s/tim-type/tim-types", baseUrl);
        TimType[] ttArr = new TimType[1];
        TimType tt = new TimType();
        tt.setDescription("description");
        tt.setTimTypeId(-1l);
        tt.setType("type");
        ttArr[0] = tt;
        doReturn(ttArr).when(mockResponseEntityTimTypeArray).getBody();
        doReturn(mockResponseEntityTimTypeArray).when(mockRestTemplate).getForEntity(url, TimType[].class);

        // Act
        List<TimType> data = uut.selectAll();

        // Assert
        verify(mockRestTemplate).getForEntity(url, TimType[].class);
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals(tt, data.get(0));
    }
}