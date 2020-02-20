package com.trihydro.mongologger.app;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trihydro.mongologger.app.loggers.MongoLogger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OdeMongoLoggingConsumer {

	static PreparedStatement preparedStatement = null;
	static Statement statement = null;
	static ObjectMapper mapper;
	private BasicConfiguration configProperties;

	@Autowired
	public OdeMongoLoggingConsumer(BasicConfiguration configProperties) throws IOException, SQLException {
		this.configProperties = configProperties;
		MongoLogger.setConfig(configProperties);

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
				try {
					while (true) {
						ConsumerRecords<String, String> records = stringConsumer.poll(100);
						ArrayList<String> recStrings = new ArrayList<String>();
						for (ConsumerRecord<String, String> record : records) {
							recStrings.add(record.value());
						}

						String[] recStringArr = recStrings.toArray(new String[recStrings.size()]);

						if (topic.equals("topic.OdeTimJson")) {
							MongoLogger.logTim(recStringArr);
						} else if (topic.equals("topic.OdeBsmJson")) {
							MongoLogger.logBsm(recStringArr);
						} else if (topic.equals("topic.OdeDriverAlertJson")) {
							MongoLogger.logDriverAlert(recStringArr);
						}
					}
				} finally {
					stringConsumer.close();
				}
			}
		}).start();
	}
}