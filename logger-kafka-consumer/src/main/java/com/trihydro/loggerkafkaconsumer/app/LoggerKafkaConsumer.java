package com.trihydro.loggerkafkaconsumer.app;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.TopicDataWrapper;
import com.trihydro.loggerkafkaconsumer.app.dataConverters.BsmDataConverter;
import com.trihydro.loggerkafkaconsumer.app.dataConverters.DriverAlertDataConverter;
import com.trihydro.loggerkafkaconsumer.app.dataConverters.TimDataConverter;
import com.trihydro.loggerkafkaconsumer.app.services.BsmService;
import com.trihydro.loggerkafkaconsumer.app.services.DriverAlertService;
import com.trihydro.loggerkafkaconsumer.app.services.TimService;
import com.trihydro.loggerkafkaconsumer.config.LoggerConfiguration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.model.OdeData;

@Component
public class LoggerKafkaConsumer {

    private ObjectMapper mapper;
    private LoggerConfiguration loggerConfig;
    private BsmService bsmService;
    private TimService timService;
    private DriverAlertService driverAlertService;
    private TimDataConverter timDataConverter;
    private BsmDataConverter bsmDataConverter;
    private DriverAlertDataConverter daConverter;
    private Utility utility;

    @Autowired
    public LoggerKafkaConsumer(LoggerConfiguration _loggerConfig, BsmService _bsmService, TimService _timService,
            DriverAlertService _driverAlertService, TimDataConverter _timDataConverter,
            BsmDataConverter _bsmDataConverter, DriverAlertDataConverter _daConverter, Utility _utility)
            throws IOException, Exception {
        loggerConfig = _loggerConfig;
        bsmService = _bsmService;
        timService = _timService;
        driverAlertService = _driverAlertService;
        timDataConverter = _timDataConverter;
        bsmDataConverter = _bsmDataConverter;
        daConverter = _daConverter;
        utility = _utility;

        System.out.println("starting..............");

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        startKafkaConsumer();
    }

    public void startKafkaConsumer() throws Exception {

        String endpoint = loggerConfig.getKafkaHostServer() + ":9092";

        // Properties for the kafka topic
        Properties props = new Properties();
        props.put("bootstrap.servers", endpoint);
        props.put("group.id", loggerConfig.getDepositGroup());
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("max.poll.interval.ms", loggerConfig.getMaxPollIntervalMs());
        props.put("max.poll.records", loggerConfig.getMaxPollRecords());
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> stringConsumer = new KafkaConsumer<String, String>(props);

        String topic = loggerConfig.getDepositTopic();

        stringConsumer.subscribe(Arrays.asList(topic));
        System.out.println("Subscribed to topic " + topic);

        Gson gson = new Gson();

        try {
            OdeData odeData = new OdeData();
            while (true) {
                ConsumerRecords<String, String> records = stringConsumer.poll(100);
                var recordCount = records.count();
                if(recordCount > 0){
                    utility.logWithDate(String.format("Found %d records to parse", recordCount));
                }
                for (ConsumerRecord<String, String> record : records) {
                    TopicDataWrapper tdw = null;
                    try {
                        tdw = gson.fromJson(record.value(), TopicDataWrapper.class);
                    } catch (Exception e) {
                        // Could be ioException, JsonParseException, JsonMappingException
                        e.printStackTrace();
                    }
                    if (tdw != null && tdw.getData() != null) {
                        utility.logWithDate(String.format("Found data for topic: %s", tdw.getTopic()));
                        switch (tdw.getTopic()) {
                            case "topic.OdeTimJson":
                                odeData = timDataConverter.processTimJson(tdw.getData());
                                utility.logWithDate(String.format("Parsed TIM: %s", gson.toJson(odeData)));
                                if (odeData != null) {
                                    if (odeData.getMetadata()
                                            .getRecordGeneratedBy() == us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy.TMC) {
                                        timService.addActiveTimToOracleDB(odeData);
                                    } else {
                                        timService.addTimToOracleDB(odeData);
                                    }
                                } else {
                                    utility.logWithDate("Failed to parse topic.OdeTimJson, insert fails");
                                }
                                break;

                            case "topic.OdeBsmJson":
                                odeData = bsmDataConverter.processBsmJson(tdw.getData());
                                if (odeData != null) {
                                    bsmService.addBSMToOracleDB(odeData, tdw.getData());
                                } else {
                                    utility.logWithDate("Failed to parse topic.OdeBsmJson, insert fails");
                                }
                                break;

                            case "topic.OdeDriverAlertJson":
                                odeData = daConverter.processDriverAlertJson(tdw.getData());
                                if (odeData != null) {
                                    driverAlertService.addDriverAlertToOracleDB(odeData);
                                } else {
                                    utility.logWithDate("Failed to parse topic.OdeDriverAlertJson, insert fails");
                                }
                                break;
                        }
                    } else {
                        utility.logWithDate("Logger Kafka Consumer failed to deserialize proper TopicDataWrapper");
                        if (tdw != null) {
                            utility.logWithDate(gson.toJson(tdw));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            utility.logWithDate(ex.getMessage());
            throw (ex);
        } finally {
            try {
                stringConsumer.close();
            } catch (Exception consumerEx) {
                consumerEx.printStackTrace();
            }
        }
    }
}