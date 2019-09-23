package com.trihydro.cvlogger.app;

import java.sql.*;
import java.util.Properties;
import java.util.Arrays;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;

import com.trihydro.cvlogger.app.loggers.BsmLogger;
import com.trihydro.cvlogger.app.loggers.TimLogger;
import com.trihydro.cvlogger.app.loggers.DriverAlertLogger;

import us.dot.its.jpo.ode.model.OdeData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import com.trihydro.cvlogger.app.services.TracManager;
import com.trihydro.library.model.ConfigProperties;
import com.trihydro.library.service.CvDataServiceLibrary;

public class OdeLoggingConsumer {

	static PreparedStatement preparedStatement = null;
	static Statement statement = null;
	static ObjectMapper mapper;

	public static void main(String[] args) throws IOException, SQLException {

		Options options = new Options();

		Option topic_option = new Option("t", "topic", true, "Topic Name");
		topic_option.setRequired(true);
		options.addOption(topic_option);

		Option group_option = new Option("g", "group", true, "Consumer Group");
		group_option.setRequired(true);
		options.addOption(group_option);

		Option type_option = new Option("type", "type", true, "string|byte message type");
		type_option.setRequired(true);
		options.addOption(type_option);

		Option config_option = new Option("p", "configFile", true, "Properties file");
		group_option.setRequired(true);
		options.addOption(config_option);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("Consumer Example", options);
			System.exit(1);
			return;
		}

		String topic = cmd.getOptionValue("topic");
		String group = cmd.getOptionValue("group");
		String type = cmd.getOptionValue("type");
		String configFile = cmd.getOptionValue("configFile");

		InputStream inputStream = OdeLoggingConsumer.class.getClassLoader().getResourceAsStream(configFile);

		Properties appProps = new Properties();
		appProps.load(inputStream);

		ConfigProperties config = new ConfigProperties();

		config.setDbDriver(appProps.getProperty("dbDriver"));
		config.setDbUrl(appProps.getProperty("dbUrl"));
		config.setDbUsername(appProps.getProperty("dbUsername"));
		config.setDbPassword(appProps.getProperty("dbPassword"));
		config.setEnv(appProps.getProperty("env"));
		config.setTracUrl(appProps.getProperty("tracUrl"));

		config.setAlertAddresses(appProps.getProperty("alertAddress"));

		CvDataServiceLibrary.setConfig(config);

		System.out.println("starting..............");

		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String endpoint = appProps.getProperty("hostname") + ":9092";

		// Properties for the kafka topic
		Properties props = new Properties();
		props.put("bootstrap.servers", endpoint);
		props.put("group.id", group);
		props.put("enable.auto.commit", "false");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		if (type.equals("byte")) {
			props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
		} else {
			props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		}

		if (!type.equals("byte")) {

			KafkaConsumer<String, String> stringConsumer = new KafkaConsumer<String, String>(props);

			stringConsumer.subscribe(Arrays.asList(topic));
			System.out.println("Subscribed to topic " + topic);

			TracManager tm = new TracManager();
			try {

				while (true) {
					ConsumerRecords<String, String> records = stringConsumer.poll(100);
					for (ConsumerRecord<String, String> record : records) {
						System.out.println("Input text: " + record.value());
						if (topic.equals("topic.OdeDNMsgJson")) {
							tm.submitDNMsgToTrac(record.value(), config);
						} else if (topic.equals("topic.OdeTimJson")) {
							OdeData odeData = TimLogger.processTimJson(record.value());
							if (odeData != null) {
								if (odeData.getMetadata()
										.getRecordGeneratedBy() == us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy.TMC)
									TimLogger.addActiveTimToOracleDB(odeData);
								else {
									TimLogger.addTimToOracleDB(odeData);
								}
							}
						} else if (topic.equals("topic.OdeBsmJson")) {
							OdeData odeData = BsmLogger.processBsmJson(record.value());
							if (odeData != null)
								BsmLogger.addBSMToOracleDB(odeData, record.value());
						} else if (topic.equals("topic.OdeDriverAlertJson")) {
							OdeData odeData = DriverAlertLogger.processDriverAlertJson(record.value());
							if (odeData != null)
								DriverAlertLogger.addDriverAlertToOracleDB(odeData);
						}
					}
				}
			} finally {
				stringConsumer.close();
			}
		}
	}
}