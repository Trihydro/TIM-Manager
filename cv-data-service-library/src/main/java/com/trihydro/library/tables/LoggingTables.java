package com.trihydro.library.tables;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class LoggingTables extends DbTables {
    private List<String> httpLoggingTable;

    public List<String> getHttpLoggingTable() {
        if (httpLoggingTable == null) {
            httpLoggingTable = new ArrayList<String>();

            httpLoggingTable.add("REQUEST_TIME");
            httpLoggingTable.add("REST_REQUEST");
            httpLoggingTable.add("RESPONSE_TIME");
        }

        return httpLoggingTable;
    }
}