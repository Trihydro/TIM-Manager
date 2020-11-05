package com.trihydro.certexpiration.app;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.trihydro.certexpiration.config.CertExpirationConfiguration;
import com.trihydro.certexpiration.controller.LoopController;
import com.trihydro.certexpiration.factory.KafkaConsumerFactory;
import com.trihydro.certexpiration.model.CertExpirationModel;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.ActiveTimService;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CertExpirationConsumer {
	private CertExpirationConfiguration configProperties;
	private Utility utility;
	private ActiveTimService ats;
	private EmailHelper emailHelper;
	private LoopController loopController;
	private KafkaConsumerFactory kafkaConsumerFactory;

	@Autowired
	public CertExpirationConsumer(CertExpirationConfiguration configProperties, Utility _utility, ActiveTimService _ats,
			EmailHelper _emailHelper, LoopController _loopController, KafkaConsumerFactory _kafkaConsumerFactory)
			throws IOException, Exception {
		this.configProperties = configProperties;
		utility = _utility;
		ats = _ats;
		emailHelper = _emailHelper;
		loopController = _loopController;
		kafkaConsumerFactory = _kafkaConsumerFactory;
	}

	public void startKafkaConsumer() throws Exception {
		utility.logWithDate("starting..............");
		Consumer<String, String> stringConsumer = kafkaConsumerFactory.createConsumer();

		Gson gson = new Gson();
		Duration polTime = Duration.ofMillis(100);
		var holdingQueue = new LinkedList<ConsumerRecord<String, String>>();

		try {
			while (loopController.loop()) {
				if (holdingQueue.size() < configProperties.getMaxQueueSize()) {
					// proceed to poll and add to our holdingQueue
					ConsumerRecords<String, String> records = stringConsumer.poll(polTime);
					records.forEach(consumerRecord -> {
						holdingQueue.add(consumerRecord);
					});
				}

				if (holdingQueue.size() > 0 && shouldBeProcessed(holdingQueue.getFirst())) {
					// we have a record, try to process it
					var record = holdingQueue.pop();
					utility.logWithDate(String.format("Processing from expiration topic: %s", record.value()));

					// ok, we have a new record now:
					// use packetId, and startDateTime to locate a unique record
					// update expiration value

					// parse record.value() as CertExpirationModel
					var cem = gson.fromJson(record.value(), CertExpirationModel.class);
					// get min expiration time
					var minExp = ats.getMinExpiration(cem.getPacketID(), cem.getStartDateTime(),
							cem.getExpirationDate());

					// use the minExp to update TIM Expiration in db
					var success = ats.updateActiveTimExpiration(cem.getPacketID(), cem.getStartDateTime(), minExp);

					if (success) {
						utility.logWithDate("Successfully updated expiration date");
					} else {
						utility.logWithDate(String.format("Failed to update expiration for data: %s", record.value()));

						String body = "The CertExpirationConsumer failed attempting to update an ActiveTim record";
						body += "<br/>";
						body += "The associated expiration topic record is: <br/>";
						body += record.value();
						emailHelper.SendEmail(configProperties.getAlertAddresses(), null,
								"CertExpirationConsumer Failed To Update ActiveTim", body,
								configProperties.getMailPort(), configProperties.getMailHost(),
								configProperties.getFromEmail());
					}

					var offsets = Collections.singletonMap(
							new TopicPartition(configProperties.getDepositTopic(), record.partition()),
							new OffsetAndMetadata(record.offset()));
					stringConsumer.commitAsync(offsets, (offset, exception) -> {
						utility.logWithDate(
								String.format("CertExpiration consumer commit callback, offset: %s, exception %s%n",
										offset, exception));
					});
				}
			}
		} catch (Exception ex) {
			utility.logWithDate(ex.getMessage());
			String body = "The CertExpirationConsumer failed attempting to consume records";
			body += ". <br/>Exception message: ";
			body += ex.getMessage();
			body += "<br/>Stacktrace: ";
			body += ExceptionUtils.getStackTrace(ex);
			try {
				emailHelper.SendEmail(configProperties.getAlertAddresses(), null, "CertExpirationConsumer Failed", body,
						configProperties.getMailPort(), configProperties.getMailHost(),
						configProperties.getFromEmail());
			} catch (Exception exception) {
				utility.logWithDate("CertExpirationConsumer failed, then failed to send email");
				exception.printStackTrace();
			}
			throw (ex);
		} finally {
			try {
				stringConsumer.close();
			} catch (Exception consumerEx) {
				consumerEx.printStackTrace();
			}
		}
	}

	private boolean shouldBeProcessed(ConsumerRecord<String, String> record) {
		// compare the record.timeStamp + configProperties.getWaitTime to current time
		var date = new Date();
		var ts = new Timestamp(date.getTime());
		return record.timestamp() + configProperties.getProcessWaitTime() < ts.getTime();
	}
}