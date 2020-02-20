package com.trihydro.cvdatacontroller.tables;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TracMessageOracleTables extends OracleTables {

    private List<String> tracMessageSentTable;
    private List<String> tracMessageTypeTable;

    public List<String> getTracMessageSentTable() {
        if (tracMessageSentTable != null)
            return tracMessageSentTable;
        else {
            tracMessageSentTable = new ArrayList<String>();
            tracMessageSentTable.add("TRAC_MESSAGE_TYPE_ID");
            tracMessageSentTable.add("DATE_TIME_SENT");
            tracMessageSentTable.add("MESSAGE_TEXT");
            tracMessageSentTable.add("PACKET_ID");
            tracMessageSentTable.add("REST_RESPONSE_CODE");
            tracMessageSentTable.add("REST_RESPONSE_MESSAGE");
            tracMessageSentTable.add("MESSAGE_SENT");
            tracMessageSentTable.add("EMAIL_SENT");
            return tracMessageSentTable;
        }
    }

    public List<String> getTracMessageTypeTable() {
        if (tracMessageTypeTable != null)
            return tracMessageTypeTable;
        else {
            tracMessageTypeTable = new ArrayList<String>();
            tracMessageTypeTable.add("TRAC_MESSAGE_TYPE");
            tracMessageTypeTable.add("TRAC_MESSAGE_DESCRIPTION");
            return tracMessageTypeTable;
        }
    }
}
