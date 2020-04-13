package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.TimType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)

public class TimTypeServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<TimType[]> mockResponseEntityTimTypeArray;
    @Mock
    protected CVRestServiceProps cVRestServiceProps;

    @InjectMocks
    private TimTypeService uut;

    @Test
    public void selectAll() {
        // Arrange
        String url = "null/tim-type/tim-types";
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
        assertEquals(1, data.size());
        assertEquals(tt, data.get(0));
    }
}