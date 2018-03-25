package com.trihydro.library.tables;

import java.util.*;

public class OracleTables {
    
    public static String buildInsertQueryStatement(String tableName, List<String> table){

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
}


