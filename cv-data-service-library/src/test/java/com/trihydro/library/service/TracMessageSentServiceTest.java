package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.TracMessageSent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class TracMessageSentServiceTest extends BaseServiceTest {

    @Mock
    protected ResponseEntity<Long> mockResponseEntityLong;
    @Mock
    private ResponseEntity<String[]> mockResponseEntityStringArray;
    @Mock
    protected CVRestServiceProps mockCVRestServiceProps;

    @InjectMocks
    private TracMessageSentService uut;

    private String baseUrl = "baseUrl";

    @Test
    public void selectPacketIds() {
        // Arrange
        doReturn(baseUrl).when(mockCVRestServiceProps).getCvRestService();

        String url = String.format("%s/trac-message/packet-ids", baseUrl);
        String[] stringArr = new String[1];
        stringArr[0] = "test";
        doReturn(stringArr).when(mockResponseEntityStringArray).getBody();
        doReturn(mockResponseEntityStringArray).when(mockRestTemplate).getForEntity(url, String[].class);

        // Act
        List<String> data = uut.selectPacketIds();

        // Assert
        verify(mockRestTemplate).getForEntity(url, String[].class);
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals("test", data.get(0));
    }

    @Test
    public void insertTracMessageSent() {
        // Arrange
        TracMessageSent tracMessageSent = new TracMessageSent();
        tracMessageSent.setMessageText("messageText");
        String url = "null/trac-message/add-trac-message-sent";
        HttpEntity<TracMessageSent> entity = getEntity(tracMessageSent, TracMessageSent.class);
        when(mockResponseEntityLong.getBody()).thenReturn(1l);
        doReturn(mockResponseEntityLong).when(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);

        // Act
        Long data = uut.insertTracMessageSent(tracMessageSent);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);
        Assertions.assertEquals(Long.valueOf(1), data);
    }
}