package com.cvlogger.app.services;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import com.trihydro.service.tracmessage.TracMessageSentService;
import com.trihydro.service.tracmessage.TracMessageTypeService;
import com.trihydro.service.model.TracMessageSent;
import com.trihydro.service.model.TracMessageType;

public class TracManager {
    
    public static TracMessageSent isDnMsgInTrac(Connection connection, String packetId){
        
        List<TracMessageSent> tracMessagesSent = TracMessageSentService.selectAll(connection);

        TracMessageSent tracMessageSent = tracMessagesSent.stream()
            .filter(x -> x.getPacketId().equals(packetId))
            .findFirst()
            .orElse(null);

        return tracMessageSent;
    }

    public static Long logNewDistressNotification(Connection connection, String packetId, String messageText){
               
        // get trac message types
        List<TracMessageType> tracMessageTypes = TracMessageTypeService.selectAll(connection);

        // get message type equal to distress notification
        TracMessageType tracMessageType = tracMessageTypes.stream()
        .filter(x -> x.getTracMessageType().equals("DN"))
        .findFirst()
        .orElse(null);
        
        TracMessageSent tracMessageSent = new TracMessageSent();
		tracMessageSent.setTracMessageTypeId(tracMessageType.getTracMessageTypeId());
		tracMessageSent.setDateTimeSent(new Timestamp(System.currentTimeMillis()));
		tracMessageSent.setMessageText(messageText);
        tracMessageSent.setPacketId(packetId);
        System.out.println("packet id: " + packetId);
        // log in db
        Long tracMessageSentId = TracMessageSentService.insertTracMessageSent(tracMessageSent, connection);
        return tracMessageSentId;
    }
}