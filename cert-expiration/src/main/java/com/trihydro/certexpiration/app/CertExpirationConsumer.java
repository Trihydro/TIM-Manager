package com.trihydro.certexpiration.app;

import java.io.IOException;
import java.time.Duration;

import com.google.gson.Gson;
import com.trihydro.certexpiration.config.CertExpirationConfiguration;
import com.trihydro.certexpiration.controller.LoopController;
import com.trihydro.library.factory.KafkaFactory;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.TopicDataWrapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CertExpirationConsumer {
    private CertExpirationConfiguration configProperties;
	private Utility utility;
	private EmailHelper emailHelper;
	private LoopController loopController;
	private KafkaFactory kafkaFactory;

	@Autowired
	public CertExpirationConsumer(CertExpirationConfiguration configProperties, Utility _utility,
			EmailHelper _emailHelper, LoopController _loopController, KafkaFactory _kafkaFactory)
			throws IOException, Exception {
		this.configProperties = configProperties;
		utility = _utility;
		emailHelper = _emailHelper;
		loopController = _loopController;
		kafkaFactory = _kafkaFactory;
	}

	public void startKafkaConsumer() throws Exception {
        log.info("starting..............");
		var stringConsumer = kafkaFactory.createStringConsumer(configProperties.getKafkaHostServer() + ":9092",
				configProperties.getDepositGroup(), configProperties.getDepositTopic());
		var stringProducer = kafkaFactory.createStringProducer(configProperties.getKafkaHostServer() + ":9092");

		Gson gson = new Gson();
		Duration polTime = Duration.ofMillis(100);
		String producerTopic = configProperties.getProducerTopic();

		try {
			while (loopController.loop()) {
				ConsumerRecords<String, String> records = stringConsumer.poll(polTime);
				for (var record : records) {
					String logTxt = String.format("Found topic %s, submitting to %s for later consumption",
							record.topic(), producerTopic);
                    log.info(logTxt);

					TopicDataWrapper tdw = new TopicDataWrapper();
					tdw.setTopic(record.topic());
					tdw.setData(record.value());
					ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(producerTopic,
							gson.toJson(tdw));
					stringProducer.send(producerRecord);
				}
			}
		} catch (Exception ex) {
            log.info(ex.getMessage());
			emailHelper.ContainerRestarted(configProperties.getAlertAddresses(), configProperties.getMailPort(),
					configProperties.getMailHost(), configProperties.getFromEmail(), "Logger Kafka Consumer");
			// Re-throw exception to cause container to exit and restart
			throw ex;
		} finally {
			try {
				stringConsumer.close();
				stringProducer.close();
			} catch (Exception consumerEx) {
                log.error("Exception", consumerEx);
			}
		}
	}
}