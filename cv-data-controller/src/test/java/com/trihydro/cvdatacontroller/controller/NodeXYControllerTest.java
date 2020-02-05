package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.Attributes;

@RunWith(MockitoJUnitRunner.class)
public class NodeXYControllerTest extends TestBase<NodeXYController> {

    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Spy
    private TimOracleTables mockTimOracleTables;

    @Before
    public void setupSubTest() throws SQLException {
        doReturn(mockPreparedStatement).when(mockTimOracleTables).buildUpdateStatement(any(), any(), any(), any(),
                any());
        doReturn("").when(mockTimOracleTables).buildInsertQueryStatement(any(), any());
        doReturn(mockPreparedStatement).when(mockConnection).prepareStatement("", new String[] { "region_id" });
        doReturn(true).when(uut).updateOrDelete(mockPreparedStatement);
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    @Test
    public void AddNodeXY_SUCCESS() throws SQLException {
        // Arrange
        OdeTravelerInformationMessage.NodeXY nodeXY = new OdeTravelerInformationMessage.NodeXY();
        Attributes attributes = new Attributes();
        nodeXY.setAttributes(attributes);
        // Act
        ResponseEntity<Long> data = uut.AddNodeXY(nodeXY);

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 1, nodeXY.getDelta());// DELTA
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 2, nodeXY.getNodeLat());// NODE_LAT
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 3, nodeXY.getNodeLong());// NODE_LONG
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 4, nodeXY.getX());// X
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 5, nodeXY.getY());// Y
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 6, nodeXY.getAttributes().getdWidth());// ATTRIBUTES_DWIDTH
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 7,
                nodeXY.getAttributes().getdElevation());// ATTRIBUTES_DELEVATION
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void AddNodeXY_FAIL() throws SQLException {
        // Arrange
        OdeTravelerInformationMessage.NodeXY nodeXY = new OdeTravelerInformationMessage.NodeXY();
        doThrow(new SQLException()).when(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 1,
                nodeXY.getDelta());
        // Act
        ResponseEntity<Long> data = uut.AddNodeXY(nodeXY);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

}