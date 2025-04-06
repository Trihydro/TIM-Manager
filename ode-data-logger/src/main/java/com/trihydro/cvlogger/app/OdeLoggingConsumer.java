package com.trihydro.cvlogger.app;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.trihydro.cvlogger.config.DataLoggerConfiguration;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.TopicDataWrapper;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OdeLoggingConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(OdeLoggingConsumer.class);

    static PreparedStatement preparedStatement = null;
	static Statement statement = null;
	private DataLoggerConfiguration configProperties;
	private Utility utility;
	private EmailHelper emailHelper;

	@Autowired
	public OdeLoggingConsumer(DataLoggerConfiguration configProperties, Utility _utility,
			EmailHelper _emailHelper) throws IOException, Exception {
		this.configProperties = configProperties;
		utility = _utility;
		emailHelper = _emailHelper;
        LOG.info("starting..............");
		setupTopic();
		startKafkaConsumer();
	}

	public void setupTopic() {

		String endpoint = configProperties.getKafkaHostServer() + ":9092";
		Properties properties = new Properties();
		properties.put("bootstrap.servers", endpoint);
		properties.put("group.id", configProperties.getDepositGroup());
		properties.put("auto.commit.interval.ms", "1000");
		properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		var admin = Admin.create(properties);
		var listTopics = admin.listTopics();
		try {
			var names = listTopics.names().get();
			if (names != null && !names.contains(configProperties.getProducerTopic())) {
				// topic doesn't exist, create it
				NewTopic newTopic = new NewTopic(configProperties.getProducerTopic(), 1, (short) 1);
				List<NewTopic> newTopics = new ArrayList<NewTopic>();
				newTopics.add(newTopic);
				admin.createTopics(newTopics);

			}
		} catch (InterruptedException e) {
            LOG.error("Exception", e);
			return;
		} catch (ExecutionException e) {
            LOG.error("Exception", e);
			return;
		} finally {
			admin.close();
		}
	}

	public void startKafkaConsumer() throws Exception {
		String endpoint = configProperties.getKafkaHostServer() + ":9092";

		// Properties for the kafka topic
		Properties consumerProps = new Properties();
		consumerProps.put("bootstrap.servers", endpoint);
		consumerProps.put("group.id", configProperties.getDepositGroup());
		consumerProps.put("auto.commit.interval.ms", "1000");
		consumerProps.put("session.timeout.ms", "30000");
		consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		KafkaConsumer<String, String> stringConsumer = new KafkaConsumer<String, String>(consumerProps);
		String consumerTopic = configProperties.getDepositTopic();
		stringConsumer.subscribe(Arrays.asList(consumerTopic));
        LOG.info("Subscribed to topic {}", consumerTopic);

		Properties producerProps = new Properties();
		producerProps.put("bootstrap.servers", endpoint);
		producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		KafkaProducer<String, String> stringProducer = new KafkaProducer<String, String>(producerProps);
		String producerTopic = configProperties.getProducerTopic();

		Gson gson = new Gson();

		try {
			while (true) {
				Duration polTime = Duration.ofMillis(100);
				ConsumerRecords<String, String> records = stringConsumer.poll(polTime);
				for (ConsumerRecord<String, String> record : records) {
					String logTxt = String.format("Found topic %s, submitting to %s for later consumption",
							record.topic(), producerTopic);
                    LOG.info(logTxt);
					TopicDataWrapper tdw = new TopicDataWrapper();
					tdw.setTopic(record.topic());
					tdw.setData(record.value());
					ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(
							producerTopic, gson.toJson(tdw));
					stringProducer.send(producerRecord);
				}
			}
		} catch (Exception ex) {
            LOG.info(ex.getMessage());
			emailHelper.ContainerRestarted(configProperties.getAlertAddresses(), configProperties.getMailPort(),
					configProperties.getMailHost(), configProperties.getFromEmail(), consumerTopic + " Consumer");
			throw (ex);
		} finally {
			try {
				stringConsumer.close();
			} catch (Exception consumerEx) {
                LOG.error("Exception", consumerEx);
			}
			try {
				stringProducer.close();
			} catch (Exception producerEx) {
                LOG.error("Exception", producerEx);
			}
		}
	}
}