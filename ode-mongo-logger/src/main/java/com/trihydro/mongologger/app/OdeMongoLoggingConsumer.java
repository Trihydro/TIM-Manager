package com.trihydro.mongologger.app;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import com.trihydro.mongologger.app.loggers.MongoLogger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OdeMongoLoggingConsumer {

	PreparedStatement preparedStatement = null;
	Statement statement = null;
	private MongoLoggerConfiguration mongoLoggerConfig;
	private MongoLogger mongoLogger;

	@Autowired
	public OdeMongoLoggingConsumer(MongoLoggerConfiguration _mongoLoggerConfig, MongoLogger _mongoLogger)
			throws IOException, SQLException {
		this.mongoLoggerConfig = _mongoLoggerConfig;
		mongoLogger = _mongoLogger;

		System.out.println("starting..............");
		startKafkaConsumerAsync();
	}

	public void startKafkaConsumerAsync() {
		// An Async task always executes in new thread
		new Thread(new Runnable() {
			public void run() {
				String endpoint = mongoLoggerConfig.getHostname() + ":9092";

				// Properties for the kafka topic
				Properties props = new Properties();
				props.put("bootstrap.servers", endpoint);
				props.put("group.id", mongoLoggerConfig.getDepositGroup());
				props.put("auto.commit.interval.ms", "1000");
				props.put("session.timeout.ms", "30000");
				props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
				props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

				KafkaConsumer<String, String> stringConsumer = new KafkaConsumer<String, String>(props);
				String topic = mongoLoggerConfig.getDepositTopic();

				stringConsumer.subscribe(Arrays.asList(topic));
				System.out.println("Subscribed to topic " + topic);
				try {
					while (true) {
						ConsumerRecords<String, String> records = stringConsumer.poll(100);
						ArrayList<String> recStrings = new ArrayList<String>();
						for (ConsumerRecord<String, String> record : records) {
							recStrings.add(record.value());
						}

						if (recStrings.size() > 0) {
							String[] recStringArr = recStrings.toArray(new String[recStrings.size()]);

							if (topic.equals("topic.OdeTimJson")) {
								mongoLogger.logTim(recStringArr);
							} else if (topic.equals("topic.OdeBsmJson")) {
								mongoLogger.logBsm(recStringArr);
							} else if (topic.equals("topic.OdeDriverAlertJson")) {
								mongoLogger.logDriverAlert(recStringArr);
							}
						}
					}
				} catch (Exception ex) {
					Date date = new Date();
					System.out.println(date + " " + ex.getMessage());
				} finally {
					stringConsumer.close();
				}
			}
		}).start();
	}
}