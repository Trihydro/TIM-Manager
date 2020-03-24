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
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PathNodeXYServiceTest extends TestBase<PathNodeXYService> {

    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    @Test
    public void insertPathNodeXY_SUCCESS() throws SQLException {
        // Arrange

        // Act
        Long data = uut.insertPathNodeXY(-1l, -1l);

        // Assert
        assertEquals(new Long(-1), data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);// NODE_XY_ID
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 2, -1l);// PATH_ID
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void insertPathNodeXY_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);
        // Act
        Long data = uut.insertPathNodeXY(-1l, -1l);

        // Assert
        assertEquals(new Long(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

}