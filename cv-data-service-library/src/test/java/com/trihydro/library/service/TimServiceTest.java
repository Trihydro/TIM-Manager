package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import com.trihydro.library.model.TimInsertModel;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

@RunWith(PowerMockRunner.class)
public class TimServiceTest extends BaseServiceTest {
    @Mock
    private OdeMsgMetadata odeTimMetadata;
    @Mock
    private ReceivedMessageDetails receivedMessageDetails;
    @Mock
    private OdeTravelerInformationMessage j2735TravelerInformationMessage;
    @Mock
    private ResponseEntity<WydotOdeTravelerInformationMessage> mockWydotOdeTravelerInformationMessage;

    @Test
    public void insertTim_success() throws SQLException {
        HttpEntity<TimInsertModel> entity = getEntity(null, TimInsertModel.class);
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

    @Test
    public void getTim() {
        // Arrange
        Long timId = -1l;
        String url = String.format("null/get-tim/%d", timId);
        when(mockWydotOdeTravelerInformationMessage.getBody()).thenReturn(new WydotOdeTravelerInformationMessage());
        when(mockRestTemplate.getForEntity(url, WydotOdeTravelerInformationMessage.class))
                .thenReturn(mockWydotOdeTravelerInformationMessage);

        // Act
        WydotOdeTravelerInformationMessage data = TimService.getTim(timId);

        // Assert
        verify(mockRestTemplate).getForEntity(url, WydotOdeTravelerInformationMessage.class);
        assertNotNull(data);
    }
}