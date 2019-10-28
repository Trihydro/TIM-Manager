package com.trihydro.library.tables;

import java.util.*;

public class LoggingTables extends OracleTables {
    private static List<String> httpLoggingTable;

    public static List<String> getHttpLoggingTable() {
        if (httpLoggingTable == null) {
            httpLoggingTable = new ArrayList<String>();

            httpLoggingTable.add("REQUEST_TIME");
            httpLoggingTable.add("REST_REQUEST");
            httpLoggingTable.add("RESPONSE_TIME");
        }

        return httpLoggingTable;
    }
}