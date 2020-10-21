package com.trihydro.certexpiration.app;

import java.io.IOException;
import java.time.Duration;

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

		try {
			while (loopController.loop()) {
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

						String body = "The CertExpirationConsumer failed attempting to update an ActiveTim record";
						body += "<br/>";
						body += "The associated expiration topic record is: <br/>";
						body += record.value();
						emailHelper.SendEmail(configProperties.getAlertAddresses(), null,
								"CertExpirationConsumer Failed To Update ActiveTim", body,
								configProperties.getMailPort(), configProperties.getMailHost(),
								configProperties.getFromEmail());
					}
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
}