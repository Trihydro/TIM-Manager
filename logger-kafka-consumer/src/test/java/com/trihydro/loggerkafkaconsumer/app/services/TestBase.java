package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestBase<T> {
    @Mock
    protected Connection mockConnection;
    @Mock
    protected Statement mockStatement;
    @Mock
    protected PreparedStatement mockPreparedStatement;
    @Mock
    protected ResultSet mockRs;
    @Mock
    protected SQLException sqlException;

    protected T uut;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]
                .getTypeName();
        Class<?> clazz = Class.forName(className);
        uut = spy((T) clazz.newInstance());
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(isA(String.class))).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(isA(String.class), isA(String[].class))).thenReturn(mockPreparedStatement);
        doReturn(mockConnection).when((BaseService) uut).GetConnectionPool();
        doReturn(-1l).when((BaseService) uut).log(isA(PreparedStatement.class), isA(String.class));
        when(mockStatement.executeQuery(isA(String.class))).thenReturn(mockRs);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true).thenReturn(false);
    }
}