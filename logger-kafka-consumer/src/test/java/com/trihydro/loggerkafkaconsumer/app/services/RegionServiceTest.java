package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;

public class RegionServiceTest extends TestBase<RegionService> {

    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    @Test
    public void AddRegion_SUCCESS() throws SQLException {
        // Arrange
        Long dataFrameId = -1l;
        Long pathId = -2l;
        Region region = new Region();
        OdePosition3D anchor = new OdePosition3D();
        anchor.setLatitude(new BigDecimal(-1));
        anchor.setLongitude(new BigDecimal(-2));
        region.setAnchorPosition(anchor);

        // Act
        Long data = uut.AddRegion(dataFrameId, pathId, region);

        // Assert
        assertEquals(new Long(-1), data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, dataFrameId);// DATA_FRAME_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, region.getName());// NAME
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 3, region.getLaneWidth());// LANE_WIDTH
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 4, region.getDirectionality());// DIRECTIONALITY
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 5, region.getDirection());// DIRECTION
        verify(mockPreparedStatement).setBoolean(6, region.isClosedPath());// CLOSED_PATH
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 7, anchor.getLatitude());// ANCHOR_LAT
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 8, anchor.getLongitude());// ANCHOR_LONG
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 9, pathId);// PATH_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 10, null);// GEOMETRY_DIRECTION
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 11, null);// GEOMETRY_EXTENT
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 12, (BigDecimal) null);// GEOMETRY_LANE_WIDTH
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 13, (BigDecimal) null);// GEOMETRY_CIRCLE_POSITION_LAT
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 14, (BigDecimal) null);// GEOMETRY_CIRCLE_POSITION_LONG
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 15, (BigDecimal) null);// GEOMETRY_CIRCLE_POSITION_ELEV
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 16, null);// GEOMETRY_CIRCLE_RADIUS
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 17, null);// GEOMETRY_CIRCLE_UNITS
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void AddRegion_FAIL() throws SQLException {
        // Arrange
        Long dataFrameId = -1l;
        Long pathId = -2l;
        Region region = new Region();
        OdePosition3D anchor = new OdePosition3D();
        anchor.setLatitude(new BigDecimal(-1));
        anchor.setLongitude(new BigDecimal(-2));
        region.setAnchorPosition(anchor);
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, dataFrameId);

        // Act
        Long data = uut.AddRegion(dataFrameId, pathId, region);

        // Assert
        assertEquals(new Long(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}