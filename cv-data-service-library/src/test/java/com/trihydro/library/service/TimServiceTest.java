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
@PrepareForTest({ DbUtility.class, SQLNullHandler.class, SecurityResultCodeTypeService.class })
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
    private ResultSet mockRs;
    @Mock
    private SQLException sqlException;

    @Before
    public void setup() throws SQLException {
        PowerMockito.mockStatic(DbUtility.class);
        PowerMockito.mockStatic(SQLNullHandler.class);
        PowerMockito.mockStatic(SecurityResultCodeTypeService.class);
        Mockito.when(DbUtility.getConnectionPool()).thenReturn(mockConnection);
        Mockito.when(mockConnection.prepareStatement(isA(String.class), isA(String[].class))).thenReturn(mockStatement);

        // Setup the static SecurityResultCodeTypeService.getSecurityResultCodeTypes to
        // return a basic list with a single type (unknown, id -1)
        List<SecurityResultCodeType> securityResultCodeTypes = new ArrayList<SecurityResultCodeType>();
        SecurityResultCodeType srct = new SecurityResultCodeType();
        srct.setSecurityResultCodeType(SecurityResultCode.unknown.toString());
        srct.setSecurityResultCodeTypeId(-1);
        securityResultCodeTypes.add(srct);
        Mockito.when(SecurityResultCodeTypeService.getSecurityResultCodeTypes()).thenReturn(securityResultCodeTypes);
    }

    private void setupPreparedStatementForString() throws SQLException {
        Mockito.when(mockConnection.prepareStatement(isA(String.class))).thenReturn(preppedStatementReturnsSomething);
        Mockito.when(preppedStatementReturnsSomething.executeQuery()).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(true);
    }

    @Test
    public void insertTim_uniqueViolation() throws SQLException {
        setupPreparedStatementForString();
        Mockito.when(mockRs.getLong("tim_id")).thenReturn(new Long(999));
        Mockito.when(mockStatement.executeUpdate()).thenThrow(sqlException);

        String logFileName = "unit test log";
        String satRecordId = "TEST";
        String regionName = "TEST";
        SecurityResultCode securityResultCode = SecurityResultCode.unknown;// set unknown to pull from the mocked
                                                                           // function
        RecordType recordType = RecordType.rxMsg;
        Long tim_id = TimService.insertTim(odeTimMetadata, receivedMessageDetails, j2735TravelerInformationMessage,
                recordType, logFileName, securityResultCode, satRecordId, regionName);
        assertEquals(new Long(999), tim_id);
    }
}