package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
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
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.Attributes;

@RunWith(StrictStubs.class)
public class NodeXYServiceTest extends TestBase<NodeXYService> {

    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    @Test
    public void AddNodeXY_SUCCESS() throws SQLException {
        // Arrange
        OdeTravelerInformationMessage.NodeXY nodeXY = new OdeTravelerInformationMessage.NodeXY();
        Attributes attributes = new Attributes();
        nodeXY.setAttributes(attributes);
        // Act
        Long data = uut.AddNodeXY(nodeXY);

        // Assert
        assertEquals(Long.valueOf(-1), data);
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
        Long data = uut.AddNodeXY(nodeXY);

        // Assert
        assertEquals(Long.valueOf(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}