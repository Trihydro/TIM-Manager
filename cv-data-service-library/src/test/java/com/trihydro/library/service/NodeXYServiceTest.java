package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

@RunWith(PowerMockRunner.class)

public class NodeXYServiceTest extends BaseServiceTest {

    @Test
    public void insertNodeXY() {
        // Arrange
        OdeTravelerInformationMessage.NodeXY nodeXY = new OdeTravelerInformationMessage.NodeXY();
        nodeXY.setDelta("delta");
        nodeXY.setNodeLat(new BigDecimal(1));
        HttpEntity<OdeTravelerInformationMessage.NodeXY> entity = getEntity(nodeXY,
                OdeTravelerInformationMessage.NodeXY.class);
        when(mockRestTemplate.exchange("null/nodexy/add-nodexy", HttpMethod.POST, entity, Long.class))
                .thenReturn(mockResponseEntityLong);

        // Act
        Long data = NodeXYService.insertNodeXY(nodeXY);

        // Assert
        verify(mockRestTemplate).exchange("null/nodexy/add-nodexy", HttpMethod.POST, entity, Long.class);
        assertEquals(new Long(1), data);
    }
}