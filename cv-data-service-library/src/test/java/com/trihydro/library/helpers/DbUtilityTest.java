package com.trihydro.library.helpers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import javax.mail.MessagingException;

import com.trihydro.library.model.ConfigProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mail.MailException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EmailHelper.class, DbUtility.class })
public class DbUtilityTest {
    @Mock
    ConfigProperties mockConfig;
    @Mock
    HikariConfig mockHikariConfig;
    @Mock
    HikariDataSource mockHikariDataSource;
    @Mock
    SQLException sqlException;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() throws SQLException, Exception {
        PowerMockito.mockStatic(EmailHelper.class);
        PowerMockito.whenNew(HikariConfig.class).withNoArguments().thenReturn(mockHikariConfig);
        PowerMockito.whenNew(HikariDataSource.class).withAnyArguments().thenReturn(mockHikariDataSource);

        when(mockConfig.getDbUsername()).thenReturn("unit@test.user");
        when(mockConfig.getDbPassword()).thenReturn("password");
        when(mockConfig.getDbUrl()).thenReturn("url");
        when(mockConfig.getDbDriver()).thenReturn("driver");
        when(mockConfig.getEnv()).thenReturn("unit_test");
        String[] addresses = new String[1];
        addresses[0] = "from_user@test.com";
        when(mockConfig.getAlertAddresses()).thenReturn(addresses);

        DbUtility.setConfig(mockConfig);
    }

    @Test
    public void getConnectionPool_throwsException() throws SQLException, MailException, MessagingException {
        // Arrange
        when(sqlException.getMessage()).thenReturn("unit test exception");
        when(mockHikariDataSource.getConnection()).thenThrow(sqlException);

        // Act
        try {
            // exception.expect(SQLException.class);
            DbUtility.getConnectionPool();
        } catch (SQLException ex) {
            assertEquals("unit test exception", ex.getMessage());            
        }

        // Assert
        verify(mockHikariConfig).addDataSourceProperty("cachePrepStmts", "true");
        verify(mockHikariConfig).setUsername(mockConfig.getDbUsername());
        verify(mockHikariConfig).setPassword(mockConfig.getDbPassword());
        verify(mockHikariConfig).setJdbcUrl(mockConfig.getDbUrl());
        verify(mockHikariConfig).setDriverClassName(mockConfig.getDbDriver());
        verify(mockHikariConfig).setMaximumPoolSize(5);
        verify(mockHikariConfig).setMaxLifetime(600000);
        PowerMockito.verifyStatic();
        String body = "The ODE Wrapper failed attempting to open a connection to ";
        body += mockConfig.getDbUrl();
        body += ". <br/>Exception message: unit test exception";
        body += "<br/>Stacktrace: ";
        EmailHelper.SendEmail(mockConfig.getAlertAddresses(), null, "ODE Wrapper Failed To Get Connection", body,
                mockConfig);
    }

}