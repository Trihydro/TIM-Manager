package com.trihydro.loggerkafkaconsumer.app;

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
import com.trihydro.loggerkafkaconsumer.app.dataConverters.TimDataConverter;
import com.trihydro.loggerkafkaconsumer.app.services.ActiveTimHoldingService;
import com.trihydro.loggerkafkaconsumer.app.services.ActiveTimService;
import com.trihydro.loggerkafkaconsumer.app.services.TimService;
import com.trihydro.loggerkafkaconsumer.config.LoggerConfiguration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.model.OdeData;

@Component
@Slf4j
public class LoggerKafkaConsumer {

    private final LoggerConfiguration loggerConfig;
    private final KafkaFactory kafkaFactory;
    private final ActiveTimService activeTimService;
    private final ActiveTimHoldingService activeTimHoldingService;
    private final TimService timService;
    private final TimDataConverter timDataConverter;
    private final Utility utility;
    private final EmailHelper emailHelper;

    @Autowired
    public LoggerKafkaConsumer(LoggerConfiguration _loggerConfig, KafkaFactory _kafkaFactory,
                               ActiveTimService _activeTimService, TimService _timService,
                               TimDataConverter _timDataConverter, Utility _utility, EmailHelper _emailHelper,
                               ActiveTimHoldingService _activeTimHoldingService) throws Exception {
        loggerConfig = _loggerConfig;
        kafkaFactory = _kafkaFactory;
        activeTimService = _activeTimService;
        timService = _timService;
        timDataConverter = _timDataConverter;
        utility = _utility;
        emailHelper = _emailHelper;
        activeTimHoldingService = _activeTimHoldingService;

        log.info("Logger Kafka Consumer starting..............");

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        startKafkaConsumer();
    }

    public void startKafkaConsumer() throws Exception {

        String endpoint = loggerConfig.getKafkaHostServer() + ":9092";
        var stringConsumer = kafkaFactory.createStringConsumer(endpoint, loggerConfig.getDepositGroup(),
            loggerConfig.getDepositTopic(), loggerConfig.getMaxPollIntervalMs(),
            loggerConfig.getMaxPollRecords());

        Gson gson = new Gson();

        try {
            OdeData odeData;
            var recordCount = 0;
            while (true) {
                ConsumerRecords<String, String> records = stringConsumer.poll(100);
                recordCount = records.count();
                if (recordCount > 0) {
                    log.info("Found {} records to parse", recordCount);
                }
                for (ConsumerRecord<String, String> record : records) {
                    TopicDataWrapper tdw = null;
                    try {
                        tdw = gson.fromJson(record.value(), TopicDataWrapper.class);
                    } catch (Exception e) {
                        // Could be ioException, JsonParseException, JsonMappingException
                        log.error("Failed to parse record: {}", record.value(), e);
                    }
                    if (tdw != null && tdw.getData() != null) {
                        log.info("Found data for topic: {}", tdw.getTopic());
                        switch (tdw.getTopic()) {
                            case "topic.OdeTimJson":
                                log.trace("Before processing JSON: {}", tdw.getData());
                                odeData = timDataConverter.processTimJson(tdw.getData());
                                log.trace("After processing JSON: {}", gson.toJson(odeData));
                                if (odeData != null) {
                                    if (odeData.getMetadata()
                                        .getRecordGeneratedBy() == us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy.TMC) {
                                        timService.addActiveTimToDatabase(odeData);
                                    } else if (odeData.getMetadata().getRecordGeneratedBy() == null) {
                                        // we shouldn't get here...log it
                                        log.error("Failed to get recordGeneratedBy, continuing...");
                                    } else {
                                        timService.addTimToDatabase(odeData);
                                    }
                                } else {
                                    log.error("Failed to parse topic.OdeTimJson, insert fails");
                                }
                                break;

                            case "topic.OdeTIMCertExpirationTimeJson":
                                try {
                                    CertExpirationModel certExpirationModel = gson.fromJson(tdw.getData(),
                                        CertExpirationModel.class);
                                    var success = timService.updateActiveTimExpiration(certExpirationModel);
                                    if (success) {
                                        log.info("Successfully updated expiration date");
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
                                            log.info("Unable to update expiration date for Active Tim {} (Packet ID: {}). Message superseded.",
                                                activeTim.getActiveTimId(), certExpirationModel.getPacketID());
                                        }

                                        if (!success) {
                                            // Message either not superseded, or not found in active_tim nor holding tables. error case
                                            log.error("Failed to update expiration for data: {}", tdw.getData());

                                            String body = "logger-kafka-consumer failed attempting to update the expiration for an ActiveTim record";
                                            body += "<br/>";
                                            body += "The associated expiration topic record is: <br/>";
                                            body += tdw.getData();
                                            emailHelper.SendEmail(loggerConfig.getAlertAddresses(),
                                                "Failed To Update ActiveTim Expiration", body);
                                        }
                                    }
                                } catch (Exception ex) {
                                    log.error("Failed to parse topic.OdeTIMCertExpirationTimeJson, insert fails", ex);
                                }
                                break;
                        }
                    } else {
                        log.error("Logger Kafka Consumer failed to deserialize proper TopicDataWrapper");
                        if (tdw != null) {
                            log.error("Data: {}", tdw.getData());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error in Kafka Consumer: {}", ex.getMessage());
            emailHelper.ContainerRestarted(loggerConfig.getAlertAddresses(), loggerConfig.getMailPort(),
                loggerConfig.getMailHost(), loggerConfig.getFromEmail(), "Logger Kafka Consumer");
            throw ex;
        } finally {
            try {
                stringConsumer.close();
            } catch (Exception consumerEx) {
                log.error("Failed to close consumer", consumerEx);
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
            log.error("Error while checking if message was superseded: {}", ex.getMessage());
            return false;
        }
    }
}