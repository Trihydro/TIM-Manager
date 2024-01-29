package com.trihydro.cvdatacontroller.controller;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

public class PathNodeLLControllerTest extends TestBase<PathNodeLLController> {
    @Test
    public void GetNodeLLForPath_FAIL() throws SQLException {
        // Arrange
        when(mockStatement.executeQuery(isA(String.class))).thenThrow(new SQLException("Unit test exception"));

        // Act
        ResponseEntity<NodeXY[]> data = uut.GetNodeLLForPath(-1);

        // Assert
        // verify everything was closed despite error
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement).executeQuery(
                "select * from node_ll where node_ll_id in (select node_ll_id from path_node_ll where path_id = -1)");
        verify(mockRs, times(0)).getString(isA(String.class));
        verify(mockRs, times(0)).getBigDecimal(isA(String.class));
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(0, data.getBody().length);
    }

    @Test
    public void GetNodeLLForPath_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<NodeXY[]> data = uut.GetNodeLLForPath(-1);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(
                "select * from node_ll where node_ll_id in (select node_ll_id from path_node_ll where path_id = -1)");
        verify(mockRs).getString("DELTA");
        verify(mockRs).getBigDecimal("NODE_LAT");
        verify(mockRs).getBigDecimal("NODE_LONG");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        Assertions.assertEquals(1, data.getBody().length);
    }
}
