package com.trihydro.certexpiration.app;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import com.google.gson.Gson;
import com.trihydro.certexpiration.config.CertExpirationConfiguration;
import com.trihydro.certexpiration.model.CertExpirationModel;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.ActiveTimService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CertExpirationConsumer {

	static PreparedStatement preparedStatement = null;
	static Statement statement = null;
	private CertExpirationConfiguration configProperties;
	private Utility utility;
	private ActiveTimService ats;

	@Autowired
	public CertExpirationConsumer(CertExpirationConfiguration configProperties, Utility _utility, ActiveTimService _ats)
			throws IOException, Exception {
		this.configProperties = configProperties;
		utility = _utility;
		ats = _ats;
		System.out.println("starting..............");
		startKafkaConsumer();
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
		utility.logWithDate("Subscribed to topic " + consumerTopic);

		Gson gson = new Gson();

		try {
			while (true) {
				Duration polTime = Duration.ofMillis(100);
				ConsumerRecords<String, String> records = stringConsumer.poll(polTime);
				for (ConsumerRecord<String, String> record : records) {
					utility.logWithDate(String.format("Consumed from expiration topic: %s", record.value()));
					// ok, we have a new record now:
					// use packetId, and startDateTime to locate a unique record
					// update expiration value

					// parse record.value() as CertExpirationModel
					var cem = gson.fromJson(record.value(), CertExpirationModel.class);
					var success = ats.updateActiveTimExpiration(cem.getPacketID(), cem.getStartDateTime(),
							cem.getExpirationDate());

					if (success) {
						utility.logWithDate("Successfully updated expiration date");
					} else {
						utility.logWithDate(String.format("Failed to update expiration for data: %s", record.value()));
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