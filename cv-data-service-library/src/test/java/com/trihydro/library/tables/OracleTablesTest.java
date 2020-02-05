package com.trihydro.library.tables;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OracleTablesTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preppedStatement;

    @Before
    public void setup() {
        try {
            when(connection.prepareStatement(any(String.class))).thenReturn(preppedStatement);
        } catch (SQLException ex) {

        }
    }

    @Test
    public void buildUpdateStatement_singleColumn() {
        String tableName = "unitTestTable";
        String tableId = "unitTestTable_id";
        List<Pair<String, Object>> cols = new ArrayList<Pair<String, Object>>();
        cols.add(new ImmutablePair<String, Object>("NAME", "name_change"));
        Long id = new Long(-1);
        PreparedStatement ps = OracleTablesStatic.buildUpdateStatement(id, tableName, tableId, cols, connection);

        try {
            String testSql = "UPDATE unitTestTable SET NAME = ? WHERE unitTestTable_id = ?";
            verify(connection).prepareStatement(testSql);
            verify(ps).setObject(1, "name_change");
            verify(ps).setObject(2, id);
        } catch (SQLException ex) {

        }
    }

    @Test
    public void buildUpdateStatement_multipleColumns() {
        String tableName = "unitTestTable";
        String tableId = "unitTestTable_id";
        List<Pair<String, Object>> cols = new ArrayList<Pair<String, Object>>();
        cols.add(new ImmutablePair<String, Object>("NAME", "name_change"));
        cols.add(new ImmutablePair<String, Object>("COL2_NUMERIC", 27));
        Long id = new Long(-1);
        PreparedStatement ps = OracleTablesStatic.buildUpdateStatement(id, tableName, tableId, cols, connection);

        try {
            String testSql = "UPDATE unitTestTable SET NAME = ?, COL2_NUMERIC = ? WHERE unitTestTable_id = ?";
            verify(connection).prepareStatement(testSql);
            verify(ps).setObject(1, "name_change");
            verify(ps).setObject(2, 27);
            verify(ps).setObject(3, id);
        } catch (SQLException ex) {

        }
    }
}