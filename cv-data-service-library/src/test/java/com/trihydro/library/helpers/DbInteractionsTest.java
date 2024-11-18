package com.trihydro.library.helpers;

import com.trihydro.library.model.DbInteractionsProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DbInteractionsTest {

    @Mock
    private DbInteractionsProps dbConfig;

    @Mock
    private Utility utility;

    @Mock
    private EmailHelper emailHelper;

    @InjectMocks
    private DbInteractions dbInteractions;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void updateOrDelete_executesUpdate() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = dbInteractions.updateOrDelete(preparedStatement);

        assertTrue(result);
    }

    @Test
    void deleteWithPossibleZero_executesUpdate() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = dbInteractions.deleteWithPossibleZero(preparedStatement);

        assertTrue(result);
    }

    @Test
    void executeAndLog_generatesKey() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(1L);

        Long id = dbInteractions.executeAndLog(preparedStatement, "type");

        assertNotNull(id);
        assertEquals(1L, id);
    }
}