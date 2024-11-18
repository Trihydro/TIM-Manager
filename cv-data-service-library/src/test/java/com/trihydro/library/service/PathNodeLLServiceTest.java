package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import com.trihydro.library.model.CVRestServiceProps;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

public class PathNodeLLServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<NodeXY[]> mockResponseEntityNodeXYArray;

    @Mock
    private CVRestServiceProps mockConfig;

    @InjectMocks
    private PathNodeLLService uut;

    private String baseUrl = "baseUrl";

    @BeforeEach
    public void setupSubTest() {
        doReturn(baseUrl).when(mockConfig).getCvRestService();
    }

    @Test
    public void GetNodeXYForPath() {
        // Arrange
        int pathId = -1;
        String url = String.format("%s/path-node-ll/get-nodell-path/%d", baseUrl, pathId);
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
        NodeXY[] data = uut.getNodeLLForPath(pathId);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, NodeXY[].class);
        Assertions.assertEquals(nodeXY, data[0]);
    }

}
