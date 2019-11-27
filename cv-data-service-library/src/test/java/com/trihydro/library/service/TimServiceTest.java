package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbUtility.class, SQLNullHandler.class, SecurityResultCodeTypeService.class, TimOracleTables.class })
public class TimServiceTest {
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private PreparedStatement preppedStatementReturnsSomething;
    @Mock
    private OdeMsgMetadata odeTimMetadata;
    @Mock
    private ReceivedMessageDetails receivedMessageDetails;
    @Mock
    private OdeTravelerInformationMessage j2735TravelerInformationMessage;

    @Mock
    private SQLException sqlException;

    @Mock
    private ResultSet resultSet;

    @Before
    public void setup() throws SQLException {
        PowerMockito.mockStatic(DbUtility.class);
        PowerMockito.mockStatic(SQLNullHandler.class);
        PowerMockito.mockStatic(SecurityResultCodeTypeService.class);
        PowerMockito.mockStatic(TimOracleTables.class);
        Mockito.when(DbUtility.getConnectionPool()).thenReturn(mockConnection);
        Mockito.when(mockConnection.prepareStatement(isA(String.class), isA(String[].class))).thenReturn(mockStatement);
        Mockito.when(mockStatement.getGeneratedKeys()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getLong(1)).thenReturn(1L);

        // Setup the static SecurityResultCodeTypeService.getSecurityResultCodeTypes to
        // return a basic list with a single type (unknown, id -1)
        List<SecurityResultCodeType> securityResultCodeTypes = new ArrayList<SecurityResultCodeType>();
        SecurityResultCodeType srct = new SecurityResultCodeType();
        srct.setSecurityResultCodeType(SecurityResultCode.unknown.toString());
        srct.setSecurityResultCodeTypeId(-1);
        securityResultCodeTypes.add(srct);
        Mockito.when(SecurityResultCodeTypeService.getSecurityResultCodeTypes()).thenReturn(securityResultCodeTypes);
    }

    @Test
    public void insertTim_uniqueViolation() throws SQLException {
        Mockito.when(mockStatement.executeUpdate()).thenThrow(sqlException);

        String logFileName = "unit test log";
        String satRecordId = "TEST";
        String regionName = "TEST";
        SecurityResultCode securityResultCode = SecurityResultCode.unknown;// set unknown to pull from the mocked
                                                                           // function
        RecordType recordType = RecordType.rxMsg;
        Long tim_id = TimService.insertTim(odeTimMetadata, receivedMessageDetails, j2735TravelerInformationMessage,
                recordType, logFileName, securityResultCode, satRecordId, regionName);
        assertEquals(null, tim_id);
    }

    @Test
    public void insertTim_success() throws SQLException {
        Mockito.when(mockStatement.executeUpdate()).thenReturn(1);

        String logFileName = "unit test log";
        String satRecordId = "TEST";
        String regionName = "TEST";
        SecurityResultCode securityResultCode = SecurityResultCode.unknown;
        RecordType recordType = RecordType.rxMsg;

        Long tim_id = TimService.insertTim(odeTimMetadata, receivedMessageDetails, j2735TravelerInformationMessage,
                recordType, logFileName, securityResultCode, satRecordId, regionName);

        Long expected = 1L;
        assertEquals(expected, tim_id);
    }
}