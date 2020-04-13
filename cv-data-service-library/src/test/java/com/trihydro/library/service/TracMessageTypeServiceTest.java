package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.TracMessageType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)

public class TracMessageTypeServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<TracMessageType[]> mockResponseEntityTracMessageTypeArray;
    @Mock
    protected CVRestServiceProps cVRestServiceProps;
    
    @InjectMocks
    private TracMessageTypeService uut;

    @Test
    public void selectAll() {
        // Arrange
        String url = "null/trac-message-type/GetAll";
        TracMessageType[] tmtArr = new TracMessageType[1];
        TracMessageType tmt = new TracMessageType();
        tmt.setTracMessageDescription("tracMessageDescription");
        tmt.setTracMessageType("tracMessageType");
        tmtArr[0] = tmt;
        doReturn(tmtArr).when(mockResponseEntityTracMessageTypeArray).getBody();
        doReturn(mockResponseEntityTracMessageTypeArray).when(mockRestTemplate).getForEntity(url,
                TracMessageType[].class);

        // Act
        List<TracMessageType> data = uut.selectAll();

        // Assert
        verify(mockRestTemplate).getForEntity(url, TracMessageType[].class);
        assertEquals(1, data.size());
        assertEquals(tmt, data.get(0));
    }
}