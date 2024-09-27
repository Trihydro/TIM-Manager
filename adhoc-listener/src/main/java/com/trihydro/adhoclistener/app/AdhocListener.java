package com.trihydro.adhoclistener.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trihydro.library.factory.KafkaFactory;
import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.helpers.Utility;
import com.trihydro.adhoclistener.config.AdhocListenerConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdhocListener {
    private ObjectMapper mapper;
    private AdhocListenerConfiguration loggerConfig;
    private KafkaFactory kafkaFactory;
    private DbInteractions dbInteractions;
    private Utility utility;
    private boolean isRunning = true;

    @Autowired
    public AdhocListener(AdhocListenerConfiguration loggerConfig, KafkaFactory kafkaFactory, Utility utility, DbInteractions dbInteractions) {
        this.loggerConfig = loggerConfig;
        this.kafkaFactory = kafkaFactory;
        this.utility = utility;
        this.dbInteractions = dbInteractions;
        
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (loggerConfig.getListenerType().equals("kafka")) {
            startKafkaConsumer();
        }
        else if (loggerConfig.getListenerType().equals("listen-notify")) {
            try {
                startListenNotify();
            } catch (SQLException e) {
                utility.logWithDate("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        else {
            throw new UnsupportedOperationException("Unknown listener type");
        }
    }

    public void startKafkaConsumer() {
        utility.logWithDate("Starting Kafka Consumer");
        String endpoint = loggerConfig.getKafkaHostServer() + ":9092";
        var stringConsumer = createConsumer(endpoint);
        utility.logWithDate("Listening for messages on topic: " + loggerConfig.getDepositTopic());

        try {
            var recordCount = 0;
            while (isRunning) {
                ConsumerRecords<String, String> records = stringConsumer.poll(100);
                recordCount = records.count();
                if (recordCount > 0) {
                    utility.logWithDate(String.format("Found %d records to parse", recordCount));
                }
                processRecords(records);

            }
        } catch (Exception e) {
            utility.logWithDate("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stringConsumer.close();
        }
    }

    public void startListenNotify() throws SQLException {
        // listen to the 'adhoc_listener_channel' channel in postgres
        utility.logWithDate("Starting Listen Notify");
        
        // connect to db
        Connection connection = null;
        Statement statement = null;

        connection = dbInteractions.getConnectionPool();
        statement = connection.createStatement();
        statement.execute("LISTEN adhoc_listener_channel");
        
        // listen for notifications
        while (isRunning) {
            PGNotification[] notifications = null;
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            notifications = pgConnection.getNotifications();
            if (notifications != null) {
                for (PGNotification notification : notifications) {
                    // print notification details
                    utility.logWithDate("Notification: " + notification.getName());
                    utility.logWithDate("Parameter: " + notification.getParameter());
                    utility.logWithDate("PID: " + notification.getPID());

                    JsonNode payloadObject = null;
                    try {
                        payloadObject = mapper.readTree(notification.getParameter());
                    } catch (JsonMappingException e) {
                        utility.logWithDate("Error: " + e.getMessage());
                        e.printStackTrace();
                    } catch (JsonProcessingException e) {
                        utility.logWithDate("Error: " + e.getMessage());
                        e.printStackTrace();
                    }

                    if (payloadObject == null) {
                        continue;
                    }
                    
                    processJson(payloadObject);
                }
            }
        }
    }

    private void processRecords(ConsumerRecords<String, String> records) {
        for (ConsumerRecord<String, String> record : records) {
            JsonNode jsonObject = null;
            try {
                jsonObject = mapper.readTree(record.value());
            } catch (JsonMappingException e) {
                utility.logWithDate("Error: " + e.getMessage());
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                utility.logWithDate("Error: " + e.getMessage());
                e.printStackTrace();
            }
            if (jsonObject == null) {
                continue;
            }
            var payloadObject = jsonObject.get("payload");
            processJson(payloadObject);
        }
    }

    private void processJson(JsonNode payloadObject) {
        // verify that payload contains before & after elements
        if (payloadObject.get("before") == null || payloadObject.get("after") == null) {
            utility.logWithDate("Payload does not contain before and after elements");
            return;
        }

        utility.logWithDate("=========== Adhoc Listener Event ===========");        
        if (wasRecordCreated(payloadObject)) {
            handleRecordCreated(payloadObject);
            
        } else if (wasRecordDeleted(payloadObject)) {
            handleRecordDeleted(payloadObject);

        } else if (wasRecordUpdated(payloadObject)) {
            handleRecordUpdated(payloadObject);

        } else {
            utility.logWithDate("Unknown operation");
        }

        utility.logWithDate("=========== End Of Adhoc Listener Event ===========");
    }

    private Consumer<String, String> createConsumer(String endpoint) {
        return kafkaFactory.createStringConsumer(endpoint, loggerConfig.getDepositGroup(),
                loggerConfig.getDepositTopic(), Integer.valueOf(loggerConfig.getMaxPollIntervalMs()),
                Integer.valueOf(loggerConfig.getMaxPollRecords()));
    }

    private boolean wasRecordCreated(JsonNode payloadObject) {
        return payloadObject.get("before").toString().equals("null");
    }

    private boolean wasRecordDeleted(JsonNode payloadObject) {
        return payloadObject.get("after").toString().equals("null");
    }

    private boolean wasRecordUpdated(JsonNode payloadObject) {
        return !payloadObject.get("before").toString().equals("null")
                && !payloadObject.get("after").toString().equals("null");
    }

    private void handleRecordCreated(JsonNode payloadObject) {
        utility.logWithDate("A new database record has been created");
        utility.logWithDate("New record: " + payloadObject.get("after").toString());

        // TODO: create new adhoc condition
    }

    private void handleRecordDeleted(JsonNode payloadObject) {
        utility.logWithDate("A database record has been deleted");
        utility.logWithDate("Deleted record: " + payloadObject.get("before").toString());

        // TODO: delete adhoc condition
    }

    private void handleRecordUpdated(JsonNode payloadObject) {
        utility.logWithDate("A database record has been updated");
        utility.logWithDate("Before: " + payloadObject.get("before").toString());
        utility.logWithDate("After: " + payloadObject.get("after").toString());

        // TODO: update adhoc condition
    }
}