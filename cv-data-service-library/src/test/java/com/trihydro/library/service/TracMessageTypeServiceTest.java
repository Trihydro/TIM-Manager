package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.TracMessageType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

public class TracMessageTypeServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<TracMessageType[]> mockResponseEntityTracMessageTypeArray;
    @Mock
    protected CVRestServiceProps mockCVRestServiceProps;

    @InjectMocks
    private TracMessageTypeService uut;

    private String baseUrl = "baseUrl";

    @Test
    public void selectAll() {
        // Arrange
        doReturn(baseUrl).when(mockCVRestServiceProps).getCvRestService();
        String url = String.format("%s/trac-message-type/GetAll", baseUrl);
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
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals(tmt, data.get(0));
    }
}