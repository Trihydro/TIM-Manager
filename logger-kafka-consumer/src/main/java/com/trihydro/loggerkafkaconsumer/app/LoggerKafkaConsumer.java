package com.trihydro.loggerkafkaconsumer.app;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trihydro.library.model.TopicDataWrapper;
import com.trihydro.loggerkafkaconsumer.app.services.BsmService;
import com.trihydro.loggerkafkaconsumer.config.LoggerConfiguration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoggerKafkaConsumer {

    private ObjectMapper mapper;
    private LoggerConfiguration loggerConfig;
    private BsmService bsmService;

    @Autowired
    public LoggerKafkaConsumer(LoggerConfiguration _loggerConfig, BsmService _bsmService) throws IOException {
        this.loggerConfig = _loggerConfig;
        this.bsmService = _bsmService;
        // CvDataServiceLibrary.setCVRestUrl(configProperties.getCvRestService());

        System.out.println("starting..............");

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        startKafkaConsumerAsync();
    }

    public void startKafkaConsumerAsync() {
        // An Async task always executes in new thread
        new Thread(new Runnable() {
            public void run() {
                String endpoint = loggerConfig.getHostname() + ":9092";

                // Properties for the kafka topic
                Properties props = new Properties();
                props.put("bootstrap.servers", endpoint);
                props.put("group.id", loggerConfig.getDepositGroup());
                props.put("enable.auto.commit", "false");
                props.put("auto.commit.interval.ms", "1000");
                props.put("session.timeout.ms", "30000");
                props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                KafkaConsumer<String, String> stringConsumer = new KafkaConsumer<String, String>(props);

                String topic = loggerConfig.getDepositTopic();

                stringConsumer.subscribe(Arrays.asList(topic));
                System.out.println("Subscribed to topic " + topic);

                try {

                    while (true) {
                        ConsumerRecords<String, String> records = stringConsumer.poll(100);
                        for (ConsumerRecord<String, String> record : records) {
                            ObjectMapper mapper = new ObjectMapper();
                            TopicDataWrapper tdw = null;
                            try {
                                tdw = mapper.readValue(record.value(), TopicDataWrapper.class);
                            } catch (Exception e) {
                                // Could be ioException, JsonParseException, JsonMappingException
                                e.printStackTrace();
                            }
                            if (tdw != null && tdw.getData() != null) {
                                switch (tdw.getTopic()) {
                                case "topic.OdeTimJson":
                                        // TODO: inject timLogger and insert
                                        // if (tdw.getData().getMetadata()
                                        // .getRecordGeneratedBy() ==
                                        // us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy.TMC)
                                        // timLogger.addActiveTimToOracleDB(tdw.getData());
                                        // else {
                                        // timLogger.addTimToOracleDB(tdw.getData());
                                        // }
                                    break;

                                case "topic.OdeBsmJson":
                                    bsmService.addBSMToOracleDB(tdw.getData(), tdw.getOriginalString());
                                    break;

                                case "topic.OdeDriverAlertJson":
                                    break;
                                }
                            } else {
                                // TODO: alert that something went wrong
                            }
                        }
                    }
                }
                // catch (SQLException sqlException) {
                // Utility.logWithDate("SQLException in data logger");
                // sqlException.printStackTrace();
                // }
                finally {
                    stringConsumer.close();
                }
            }
        }).start();
    }
}