package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;

@RunWith(MockitoJUnitRunner.class)
public class DataFrameServiceTest extends TestBase<DataFrameService> {

    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    @Test
    public void AddDataFrame_SUCCESS() throws SQLException, ParseException {
        // Arrange
        DataFrame dFrame = new DataFrame();
        dFrame.setStartDateTime("2020-02-03T16:00Z");
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        Date dt = df.parse(dFrame.getStartDateTime());
        Timestamp time = new Timestamp(dt.getTime());

        // Act
        Long data = uut.AddDataFrame(dFrame, -1l);

        // Assert
        assertEquals(new Long(-1), data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);// TIM_ID
        verify(mockSqlNullHandler).setShortOrNull(mockPreparedStatement, 2, dFrame.getSspTimRights());// SSP_TIM_RIGHTS
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 3, null);// FRAME_TYPE
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 4, dFrame.getDurationTime());// DURATION_TIME
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 5, dFrame.getPriority());// PRIORITY
        verify(mockSqlNullHandler).setShortOrNull(mockPreparedStatement, 6, dFrame.getSspLocationRights());// SSP_LOCATION_RIGHTS
        verify(mockSqlNullHandler).setShortOrNull(mockPreparedStatement, 7, dFrame.getSspMsgTypes());// SSP_MSG_TYPES
        verify(mockSqlNullHandler).setShortOrNull(mockPreparedStatement, 8, dFrame.getSspMsgContent());// SSP_MSG_CONTENT
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 9, dFrame.getContent());// CONTENT
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 10, dFrame.getUrl());// URL
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 11, time);// START_DATE_TIME
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void AddDataFrame_FAIL() throws SQLException, ParseException {
        // Arrange
        DataFrame dFrame = new DataFrame();
        dFrame.setStartDateTime("2020-02-03T16:00Z");
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);
        
        // Act
        Long data = uut.AddDataFrame(dFrame, -1l);

        // Assert
        assertEquals(new Long(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}