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

import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.helpers.Utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestBase<T extends BaseController> {
        // Mock internals
        @Mock
        protected Connection mockConnection;
        @Mock
        protected Statement mockStatement;
        @Mock
        protected PreparedStatement mockPreparedStatement;
        @Mock
        protected ResultSet mockRs;

        // Mock BaseController dependencies
        @Mock
        protected DbInteractions mockDbInteractions;
        @Mock
        protected Utility mockUtility;

        protected T uut;

        @SuppressWarnings("unchecked")
        @BeforeEach
        public void setup() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException,
                        IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
                String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]
                                .getTypeName();
                Class<?> clazz = Class.forName(className);
                uut = spy((T) clazz.getDeclaredConstructor().newInstance());
                lenient().when(mockConnection.createStatement()).thenReturn(mockStatement);
                lenient().when(mockConnection.prepareStatement(isA(String.class))).thenReturn(mockPreparedStatement);
                lenient().when(mockConnection.prepareStatement(isA(String.class), isA(String[].class)))
                                .thenReturn(mockPreparedStatement);
                lenient().doReturn(mockConnection).when(mockDbInteractions).getConnectionPool();
                lenient().doReturn(-1l).when(mockDbInteractions).executeAndLog(isA(PreparedStatement.class),
                                isA(String.class));
                lenient().doReturn(true).when(mockDbInteractions).updateOrDelete(mockPreparedStatement);
                lenient().doReturn(true).when(mockDbInteractions).deleteWithPossibleZero(mockPreparedStatement);
                lenient().when(mockStatement.executeQuery(isA(String.class))).thenReturn(mockRs);
                lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockRs);
                lenient().when(mockRs.next()).thenReturn(true).thenReturn(false);

                uut.InjectBaseDependencies(mockDbInteractions, mockUtility);
        }
}