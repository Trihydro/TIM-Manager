package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

@RunWith(PowerMockRunner.class)

public class PathNodeXYServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<NodeXY[]> mockResponseEntityNodeXYArray;

    @Test
    public void insertPathNodeXY() {
        // Arrange
        Long nodeXYId = -1l;
        Long pathId = -2l;
        String url = String.format("null/path-node-xy/add-path-nodexy/%d/%d", nodeXYId, pathId);
        HttpEntity<String> entity = getEntity(null, String.class);
        doReturn(mockResponseEntityLong).when(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);

        // Act
        Long data = PathNodeXYService.insertPathNodeXY(nodeXYId, pathId);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);
        assertEquals(new Long(1), data);
    }

    @Test
    public void GetNodeXYForPath() {
        // Arrange
        int pathId = -1;
        String url = String.format("null/path-node-xy/get-nodexy-path/%d", pathId);
        HttpEntity<String> entity = getEntity(null, String.class);
        NodeXY[] nodeXYs = new NodeXY[1];
        NodeXY nodeXY = new NodeXY();
        nodeXY.setDelta("delta");
        nodeXY.setNodeLat(new BigDecimal(2));
        nodeXYs[0] = nodeXY;
        doReturn(nodeXYs).when(mockResponseEntityNodeXYArray).getBody();
        doReturn(mockResponseEntityNodeXYArray).when(mockRestTemplate).exchange(url, HttpMethod.GET, entity,
                NodeXY[].class);

        // Act
        NodeXY[] data = PathNodeXYService.GetNodeXYForPath(pathId);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, NodeXY[].class);
        assertEquals(nodeXY, data[0]);
    }
}