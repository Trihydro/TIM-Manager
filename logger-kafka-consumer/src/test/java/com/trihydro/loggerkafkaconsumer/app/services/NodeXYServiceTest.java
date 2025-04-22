package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.Attributes;

public class NodeXYServiceTest extends TestBase<NodeXYService> {

    @Spy
    private TimDbTables mockTimDbTables = new TimDbTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockTimDbTables, mockSqlNullHandler);
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
        Assertions.assertEquals(Long.valueOf(-1), data);
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 1, nodeXY.getDelta());// DELTA
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 2, nodeXY.getNodeLat());// NODE_LAT
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 3, nodeXY.getNodeLong());// NODE_LONG
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 4, nodeXY.getXpos());// X
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 5, nodeXY.getYpos());// Y
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 6, nodeXY.getAttributes().getDwidth());// ATTRIBUTES_DWIDTH
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 7,
                nodeXY.getAttributes().getDelevation());// ATTRIBUTES_DELEVATION
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
        Assertions.assertEquals(Long.valueOf(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}