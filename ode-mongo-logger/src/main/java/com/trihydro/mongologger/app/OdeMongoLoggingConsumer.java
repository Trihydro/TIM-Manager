package com.trihydro.mongologger.app;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.mongologger.app.loggers.MongoLogger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OdeMongoLoggingConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(OdeMongoLoggingConsumer.class);

    PreparedStatement preparedStatement = null;
	Statement statement = null;
	private MongoLoggerConfiguration mongoLoggerConfig;
	private MongoLogger mongoLogger;
	private Utility utility;
	private EmailHelper emailHelper;

	@Autowired
	public OdeMongoLoggingConsumer(MongoLoggerConfiguration _mongoLoggerConfig, MongoLogger _mongoLogger,
			Utility _utility, EmailHelper _emailHelper) throws IOException, SQLException, Exception {
		this.mongoLoggerConfig = _mongoLoggerConfig;
		mongoLogger = _mongoLogger;
		utility = _utility;
		emailHelper = _emailHelper;

        LOG.info("starting..............");
        startKafkaConsumer();
	}

	public void startKafkaConsumer() throws Exception {
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
        LOG.info("Subscribed to topic {}", topic);
		try {
			while (true) {
				ConsumerRecords<String, String> records = stringConsumer.poll(100);
				ArrayList<String> recStrings = new ArrayList<String>();
				for (ConsumerRecord<String, String> record : records) {
					recStrings.add(record.value());
				}

				if (recStrings.size() > 0) {
                    LOG.info(String.format("Found %d %s records to parse", recStrings.size(), topic));
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
            LOG.info("Exception in mongo logger application {}", ex.getMessage());
            emailHelper.ContainerRestarted(mongoLoggerConfig.getAlertAddresses(), mongoLoggerConfig.getMailPort(),
					mongoLoggerConfig.getMailHost(), mongoLoggerConfig.getFromEmail(), topic + " Mongo Consumer");
			throw (ex);
		} finally {
			try {
				stringConsumer.close();
			} catch (Exception consumerEx) {
                LOG.error("Exception", consumerEx);
			}
		}
	}
}