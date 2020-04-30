package com.trihydro.cvdatacontroller.controller;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.mockito.Mock;

public class TestBase<T> {
    @Mock
    protected Connection mockConnection;
    @Mock
    protected Statement mockStatement;
    @Mock
    protected PreparedStatement mockPreparedStatement;
    @Mock
    protected ResultSet mockRs;

    protected T uut;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]
                .getTypeName();
        Class<?> clazz = Class.forName(className);
        uut = spy((T) clazz.getDeclaredConstructor().newInstance());
        lenient().when(mockConnection.createStatement()).thenReturn(mockStatement);
        lenient().when(mockConnection.prepareStatement(isA(String.class))).thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.prepareStatement(isA(String.class), isA(String[].class))).thenReturn(mockPreparedStatement);
        lenient().doReturn(mockConnection).when((BaseController) uut).GetConnectionPool();
        lenient().doReturn(-1l).when((BaseController) uut).executeAndLog(isA(PreparedStatement.class), isA(String.class));
        lenient().doReturn(true).when((BaseController) uut).updateOrDelete(mockPreparedStatement);
        lenient().when(mockStatement.executeQuery(isA(String.class))).thenReturn(mockRs);
        lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockRs);
        lenient().when(mockRs.next()).thenReturn(true).thenReturn(false);
    }
}