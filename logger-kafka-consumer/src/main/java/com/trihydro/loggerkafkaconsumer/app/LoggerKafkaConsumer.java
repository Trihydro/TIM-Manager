package com.trihydro.loggerkafkaconsumer.app;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.trihydro.library.factory.KafkaFactory;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.CertExpirationModel;
import com.trihydro.library.model.TopicDataWrapper;
import com.trihydro.loggerkafkaconsumer.app.dataConverters.BsmDataConverter;
import com.trihydro.loggerkafkaconsumer.app.dataConverters.DriverAlertDataConverter;
import com.trihydro.loggerkafkaconsumer.app.dataConverters.TimDataConverter;
import com.trihydro.loggerkafkaconsumer.app.services.ActiveTimHoldingService;
import com.trihydro.loggerkafkaconsumer.app.services.ActiveTimService;
import com.trihydro.loggerkafkaconsumer.app.services.BsmService;
import com.trihydro.loggerkafkaconsumer.app.services.DriverAlertService;
import com.trihydro.loggerkafkaconsumer.app.services.TimService;
import com.trihydro.loggerkafkaconsumer.config.LoggerConfiguration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.model.OdeData;

@Component
public class LoggerKafkaConsumer {

    private ObjectMapper mapper;
    private LoggerConfiguration loggerConfig;
    private KafkaFactory kafkaFactory;
    private BsmService bsmService;
    private ActiveTimService activeTimService;
    private ActiveTimHoldingService activeTimHoldingService;
    private TimService timService;
    private DriverAlertService driverAlertService;
    private TimDataConverter timDataConverter;
    private BsmDataConverter bsmDataConverter;
    private DriverAlertDataConverter daConverter;
    private Utility utility;
    private EmailHelper emailHelper;

    @Autowired
    public LoggerKafkaConsumer(LoggerConfiguration _loggerConfig, KafkaFactory _kafkaFactory, BsmService _bsmService,
            ActiveTimService _activeTimService, TimService _timService, DriverAlertService _driverAlertService,
            TimDataConverter _timDataConverter, BsmDataConverter _bsmDataConverter,
            DriverAlertDataConverter _daConverter, Utility _utility, EmailHelper _emailHelper,
            ActiveTimHoldingService _activeTimHoldingService) throws IOException, Exception {
        loggerConfig = _loggerConfig;
        kafkaFactory = _kafkaFactory;
        bsmService = _bsmService;
        activeTimService = _activeTimService;
        timService = _timService;
        driverAlertService = _driverAlertService;
        timDataConverter = _timDataConverter;
        bsmDataConverter = _bsmDataConverter;
        daConverter = _daConverter;
        utility = _utility;
        emailHelper = _emailHelper;
        activeTimHoldingService = _activeTimHoldingService;

        System.out.println("starting..............");

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        startKafkaConsumer();
    }

    public void startKafkaConsumer() throws Exception {

        String endpoint = loggerConfig.getKafkaHostServer() + ":9092";
        var stringConsumer = kafkaFactory.createStringConsumer(endpoint, loggerConfig.getDepositGroup(),
                loggerConfig.getDepositTopic(), Integer.valueOf(loggerConfig.getMaxPollIntervalMs()),
                Integer.valueOf(loggerConfig.getMaxPollRecords()));

        Gson gson = new Gson();

        try {
            OdeData odeData;
            var recordCount = 0;
            while (true) {
                ConsumerRecords<String, String> records = stringConsumer.poll(100);
                recordCount = records.count();
                if (recordCount > 0) {
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
                            utility.logWithDate("Before processing JSON: " + tdw.getData());
                            odeData = timDataConverter.processTimJson(tdw.getData());
                            utility.logWithDate(String.format("Parsed TIM: %s", gson.toJson(odeData)));
                            if (odeData != null) {
                                if (odeData.getMetadata()
                                        .getRecordGeneratedBy() == us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy.TMC) {
                                    timService.addActiveTimToDatabase(odeData);
                                } else if (odeData.getMetadata().getRecordGeneratedBy() == null) {
                                    // we shouldn't get here...log it
                                    utility.logWithDate("Failed to get recordGeneratedBy, continuing...");
                                } else {
                                    timService.addTimToDatabase(odeData);
                                }
                            } else {
                                utility.logWithDate("Failed to parse topic.OdeTimJson, insert fails");
                            }
                            break;

                        case "topic.OdeBsmJson":
                            // commenting for now to prevent calling. this may be updated in the future so
                            // logic has been left in
                            // odeData = bsmDataConverter.processBsmJson(tdw.getData());
                            // if (odeData != null) {
                            // bsmService.addBSMToDatabase(odeData, tdw.getData());
                            // } else {
                            // utility.logWithDate("Failed to parse topic.OdeBsmJson, insert fails");
                            // }
                            break;

                        case "topic.OdeDriverAlertJson":
                            odeData = daConverter.processDriverAlertJson(tdw.getData());
                            if (odeData != null) {
                                driverAlertService.addDriverAlertToDatabase(odeData);
                            } else {
                                utility.logWithDate("Failed to parse topic.OdeDriverAlertJson, insert fails");
                            }
                            break;

                        case "topic.OdeTIMCertExpirationTimeJson":
                            try {
                                CertExpirationModel certExpirationModel = gson.fromJson(tdw.getData(),
                                        CertExpirationModel.class);
                                var success = timService.updateActiveTimExpiration(certExpirationModel);
                                if (success) {
                                    utility.logWithDate("Successfully updated expiration date");
                                } else {
                                    // Check for issues
                                    var activeTim = activeTimService
                                            .getActiveTimByPacketId(certExpirationModel.getPacketID());

                                    // Check if activeTim exists
                                    if (activeTim == null) {
                                        // active_tim not created yet, check active_tim_holding
                                        var ath = activeTimHoldingService
                                                .getActiveTimHoldingByPacketId(certExpirationModel.getPacketID());

                                        if (ath != null) {
                                            // update ath expiration
                                            success = activeTimHoldingService.updateTimExpiration(
                                                    certExpirationModel.getPacketID(),
                                                    certExpirationModel.getExpirationDate());
                                        }
                                    } else if (messageSuperseded(certExpirationModel.getStartDateTime(), activeTim)) {
                                        // Message superseded
                                        utility.logWithDate(String.format(
                                                "Unable to update expiration date for Active Tim %s (Packet ID: %s). Message superseded.",
                                                activeTim.getActiveTimId(), certExpirationModel.getPacketID()));
                                    }

                                    if (!success) {
                                        // Message either not superseded, or not found in active_tim nor holding tables. error case
                                        utility.logWithDate(String.format("Failed to update expiration for data: %s",
                                                tdw.getData()));

                                        String body = "logger-kafka-consumer failed attempting to update the expiration for an ActiveTim record";
                                        body += "<br/>";
                                        body += "The associated expiration topic record is: <br/>";
                                        body += tdw.getData();
                                        emailHelper.SendEmail(loggerConfig.getAlertAddresses(),
                                                "Failed To Update ActiveTim Expiration", body);
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
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
            emailHelper.ContainerRestarted(loggerConfig.getAlertAddresses(), loggerConfig.getMailPort(),
                    loggerConfig.getMailHost(), loggerConfig.getFromEmail(), "Logger Kafka Consumer");
            throw ex;
        } finally {
            try {
                stringConsumer.close();
            } catch (Exception consumerEx) {
                consumerEx.printStackTrace();
            }
        }
    }

    private boolean messageSuperseded(String startTime, ActiveTim dbRecord) {
        try {
            Date expectedStart = utility.convertDate(startTime);

            if (expectedStart == null || dbRecord.getStartTimestamp() == null) {
                return false;
            }

            // If db record bas a start time that's later than the cert expiration model's
            // start time,
            // then the TIM in question must have been updated, and the cert expiration
            // model we're
            // currently processing has been superseded.
            return expectedStart.getTime() < dbRecord.getStartTimestamp().getTime();
        } catch (Exception ex) {
            utility.logWithDate("Error while checking if message was superseded: " + ex.getMessage());
            return false;
        }
    }
}