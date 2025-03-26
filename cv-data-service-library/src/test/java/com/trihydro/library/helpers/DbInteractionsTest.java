package com.trihydro.library.helpers;

import com.trihydro.library.model.DbInteractionsProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DbInteractionsTest {
    private DbInteractions uut;

    @Mock
    private DbInteractionsProps dbConfig;

    @Mock
    private EmailHelper emailHelper;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(dbConfig.getDbUrl()).thenReturn("jdbc:postgresql://localhost:5432/test");
        when(dbConfig.getDbUsername()).thenReturn("test");
        when(dbConfig.getDbPassword()).thenReturn("test");
        when(dbConfig.getMaximumPoolSize()).thenReturn(10);
        when(dbConfig.getConnectionTimeout()).thenReturn(1000);
        uut = new DbInteractions(dbConfig, emailHelper);
    }

    @Test
    void testUpdateOrDelete_WhenExecuted_ShouldReturnTrue() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = uut.updateOrDelete(preparedStatement);

        assertTrue(result);
    }

    @Test
    void testDeleteWithPossibleZero_WhenNoRowsAffected_ShouldReturnTrue() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = uut.deleteWithPossibleZero(preparedStatement);

        assertTrue(result);
    }

    @Test
    void testExecuteAndLog_WhenKeyGenerated_ShouldReturnGeneratedKey() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(1L);

        Long id = uut.executeAndLog(preparedStatement, "type");

        assertNotNull(id);
        assertEquals(1L, id);
    }
}