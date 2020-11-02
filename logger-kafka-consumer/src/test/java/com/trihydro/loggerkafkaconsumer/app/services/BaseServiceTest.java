package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.helpers.Utility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BaseServiceTest {

    @Mock
    private DbInteractions mockDbInteractions;
    @Mock
    private Utility mockUtility;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockRs;

    @InjectMocks
    private BaseService uut;

    @BeforeEach
    public void setUp() throws Exception {
        doReturn(mockConnection).when(mockDbInteractions).getConnectionPool();
        doReturn(mockStatement).when(mockConnection).createStatement();
        doReturn(mockRs).when(mockStatement).executeQuery(any());
        when(mockRs.next()).thenReturn(true).thenReturn(false);
    }

    @Test
    public void GetSecurityResultCodeTypes_SUCCESS() throws SQLException {
        // Arrange

        // Act
        var data = uut.GetSecurityResultCodeTypes();

        // Assert
        Assertions.assertNotNull(data);
        Assertions.assertEquals(1, data.size());
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetSecurityResultCodeTypes_Exception() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getInt("SECURITY_RESULT_CODE_TYPE_ID");

        // Act
        var data = uut.GetSecurityResultCodeTypes();

        // Assert
        Assertions.assertNotNull(data);
        Assertions.assertEquals(0, data.size());
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }
}
