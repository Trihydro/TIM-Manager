package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

public class TimServiceTest extends BaseServiceTest {
    @Mock
    private OdeMsgMetadata odeTimMetadata;
    @Mock
    private ReceivedMessageDetails receivedMessageDetails;
    @Mock
    private OdeTravelerInformationMessage j2735TravelerInformationMessage;
    @Mock
    private ResponseEntity<WydotOdeTravelerInformationMessage> mockWydotOdeTravelerInformationMessage;
    @Mock
    protected CVRestServiceProps mockCVRestServiceProps;

    private String baseUrl = "baseUrl";

    @InjectMocks
    private TimService uut;

    @Test
    public void getTim() {
        // Arrange
        doReturn(baseUrl).when(mockCVRestServiceProps).getCvRestService();

        Long timId = -1l;
        String url = String.format("%s/get-tim/%d", baseUrl, timId);
        when(mockWydotOdeTravelerInformationMessage.getBody()).thenReturn(new WydotOdeTravelerInformationMessage());
        when(mockRestTemplate.getForEntity(url, WydotOdeTravelerInformationMessage.class))
                .thenReturn(mockWydotOdeTravelerInformationMessage);

        // Act
        WydotOdeTravelerInformationMessage data = uut.getTim(timId);

        // Assert
        verify(mockRestTemplate).getForEntity(url, WydotOdeTravelerInformationMessage.class);
        Assertions.assertNotNull(data);
    }
}