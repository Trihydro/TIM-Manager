package com.trihydro.cvlogger.app;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trihydro.cvlogger.app.loggers.BsmLogger;
import com.trihydro.cvlogger.app.loggers.DriverAlertLogger;
import com.trihydro.cvlogger.app.loggers.TimLogger;
import com.trihydro.cvlogger.app.services.TracManager;
import com.trihydro.cvlogger.config.DataLoggerConfiguration;
import com.trihydro.library.service.CvDataServiceLibrary;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.model.OdeData;

@Component
public class OdeLoggingConsumer {

	static PreparedStatement preparedStatement = null;
	static Statement statement = null;
	static ObjectMapper mapper;
	private DataLoggerConfiguration configProperties;
	private TimLogger timLogger;
	private BsmLogger bsmLogger;
	private DriverAlertLogger driverAlertLogger;

	@Autowired
	public OdeLoggingConsumer(DataLoggerConfiguration configProperties, TimLogger _timLogger, BsmLogger _bsmLogger, DriverAlertLogger _driverAlertLogger) throws IOException {
		this.configProperties = configProperties;
		timLogger = _timLogger;
		bsmLogger = _bsmLogger;
		driverAlertLogger = _driverAlertLogger;
		CvDataServiceLibrary.setCVRestUrl(configProperties.getCvRestService());
		System.out.println("starting..............");

		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		startKafkaConsumerAsync();
	}

	public void startKafkaConsumerAsync() {
		// An Async task always executes in new thread
		new Thread(new Runnable() {
			public void run() {
				String endpoint = configProperties.getHostname() + ":9092";

				// Properties for the kafka topic
				Properties props = new Properties();
				props.put("bootstrap.servers", endpoint);
				props.put("group.id", configProperties.getDepositGroup());
				props.put("enable.auto.commit", "false");
				props.put("auto.commit.interval.ms", "1000");
				props.put("session.timeout.ms", "30000");
				props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
				props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
				KafkaConsumer<String, String> stringConsumer = new KafkaConsumer<String, String>(props);

				String topic = configProperties.getDepositTopic();

				stringConsumer.subscribe(Arrays.asList(topic));
				System.out.println("Subscribed to topic " + topic);

				TracManager tm = new TracManager();
				try {

					while (true) {
						ConsumerRecords<String, String> records = stringConsumer.poll(100);
						for (ConsumerRecord<String, String> record : records) {
							if (topic.equals("topic.OdeDNMsgJson")) {
								tm.submitDNMsgToTrac(record.value(), configProperties);
							} else if (topic.equals("topic.OdeTimJson")) {
								OdeData odeData = timLogger.processTimJson(record.value());
								// TODO: push to kafka
								// if (odeData != null) {
								// if (odeData.getMetadata()
								// .getRecordGeneratedBy() ==
								// us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy.TMC)
								// TimLogger.addActiveTimToOracleDB(odeData);
								// else {
								// TimLogger.addTimToOracleDB(odeData);
								// }
								// }
							} else if (topic.equals("topic.OdeBsmJson")) {
								OdeData odeData = bsmLogger.processBsmJson(record.value());
								// TODO: push to kafka with original string
								// if (odeData != null)
								// BsmLogger.addBSMToOracleDB(odeData, record.value());
							} else if (topic.equals("topic.OdeDriverAlertJson")) {
								OdeData odeData = driverAlertLogger.processDriverAlertJson(record.value());
								// todo: push to kafka
								// if (odeData != null)
								// DriverAlertLogger.addDriverAlertToOracleDB(odeData);
							}
						}
					}
				} finally {
					stringConsumer.close();
				}
			}
		}).start();
	}
}