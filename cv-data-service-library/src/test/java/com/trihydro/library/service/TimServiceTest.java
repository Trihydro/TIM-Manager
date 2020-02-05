package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import com.trihydro.library.model.TimInsertModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RestTemplateProvider.class })
public class TimServiceTest {
    @Mock
    private OdeMsgMetadata odeTimMetadata;
    @Mock
    private ReceivedMessageDetails receivedMessageDetails;
    @Mock
    private OdeTravelerInformationMessage j2735TravelerInformationMessage;
    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    private ResponseEntity<Long> mockResponseEntityLong;

    @Before
    public void setup() throws SQLException {
        PowerMockito.mockStatic(RestTemplateProvider.class);
        when(RestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);
    }

    @Test
    public void insertTim_success() throws SQLException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TimInsertModel> entity = new HttpEntity<TimInsertModel>(null, headers);
        when(mockRestTemplate.exchange("null/add-tim", HttpMethod.POST, entity, Long.class))
                .thenReturn(mockResponseEntityLong);
        when(mockResponseEntityLong.getBody()).thenReturn(1l);
        String logFileName = "unit test log";
        String satRecordId = "TEST";
        String regionName = "TEST";
        SecurityResultCode securityResultCode = SecurityResultCode.unknown;
        RecordType recordType = RecordType.rxMsg;

        Long tim_id = TimService.insertTim(odeTimMetadata, receivedMessageDetails, j2735TravelerInformationMessage,
                recordType, logFileName, securityResultCode, satRecordId, regionName);

        Long expected = 1L;
        assertEquals(expected, tim_id);
    }
}