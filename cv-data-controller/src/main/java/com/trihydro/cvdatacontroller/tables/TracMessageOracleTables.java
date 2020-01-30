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
            tracMessageSentTable.add("trac_message_type_id");
            tracMessageSentTable.add("date_time_sent");
            tracMessageSentTable.add("message_text");
            tracMessageSentTable.add("packet_id");
            tracMessageSentTable.add("rest_response_code");
            tracMessageSentTable.add("rest_response_message");
            tracMessageSentTable.add("message_sent");
            tracMessageSentTable.add("email_sent");
            return tracMessageSentTable;
        }
    }

    public List<String> getTracMessageTypeTable() {
        if (tracMessageTypeTable != null)
            return tracMessageTypeTable;
        else {
            tracMessageTypeTable = new ArrayList<String>();
            tracMessageTypeTable.add("trac_message_type");
            tracMessageTypeTable.add("trac_message_description");
            return tracMessageTypeTable;
        }
    }
}
