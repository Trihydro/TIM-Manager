package com.trihydro.library.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

@Slf4j
public class DbTables {

    public String buildInsertQueryStatement(String tableName, List<String> table) {

        String insertQueryStatement = "INSERT INTO " + tableName + " (";
        String values = "VALUES (";

        for (String col : table) {
            insertQueryStatement += col + ", ";
            values += "?,";
        }

        insertQueryStatement = insertQueryStatement.substring(0, insertQueryStatement.length() - 2);
        values = values.substring(0, values.length() - 1);
        values += ")";
        insertQueryStatement += ") ";
        insertQueryStatement += values;

        return insertQueryStatement;
    }

    public PreparedStatement buildUpdateStatement(Long id, String tableName, String keyColumnName,
            List<Pair<String, Object>> table, Connection connection) {

        String updateStatement = "UPDATE " + tableName + " SET";

        Iterator<Pair<String, Object>> it = table.iterator();
        while (it.hasNext()) {
            updateStatement += " ";
            updateStatement += it.next().getKey();
            updateStatement += " = ?,";
        }
        updateStatement = updateStatement.substring(0, updateStatement.length() - 1);

        updateStatement += " WHERE ";
        updateStatement += keyColumnName;
        updateStatement += " = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(updateStatement);
            it = table.iterator();
            Integer index = 1;
            while (it.hasNext()) {
                // set the value of each item
                preparedStatement.setObject(index, it.next().getValue());
                index++;
            }

            // set the where id = ? value
            preparedStatement.setObject(index, id);
            return preparedStatement;
        } catch (SQLException ex) {
            log.warn("Error creating update statement");
            log.error("Exception", ex);
        }

        return null;
    }
}
