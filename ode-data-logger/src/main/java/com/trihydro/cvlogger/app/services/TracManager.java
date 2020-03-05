package com.trihydro.cvlogger.app.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.cvlogger.config.DataLoggerConfiguration;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.JsonToJavaConverter;
import com.trihydro.library.model.TracMessageSent;
import com.trihydro.library.model.TracMessageType;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.TracMessageSentService;
import com.trihydro.library.service.TracMessageTypeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;

@Component
public class TracManager {

	private JsonToJavaConverter jsonToJava;
	private TracMessageTypeService tracMessageTypeService;
	private TracMessageSentService tracMessageSentService;
	private JavaMailSenderImplProvider mailProvider;

	@Autowired
	public void InjectDependencies(JsonToJavaConverter _jsonToJava, TracMessageTypeService _tracMessageTypeService,
			TracMessageSentService _tracMessageSentService, JavaMailSenderImplProvider _mailProvider) {
		jsonToJava = _jsonToJava;
		tracMessageTypeService = _tracMessageTypeService;
		tracMessageSentService = _tracMessageSentService;
		mailProvider = _mailProvider;
	}

	public boolean isDnMsgInTrac(String packetId) {
		List<String> packetIds = tracMessageSentService.selectPacketIds();
		return packetIds.contains(packetId);
	}

	public Long logNewDistressNotification(String packetId, String messageText, int responseCode, String responseText,
			boolean messageSent, boolean emailSent) {

		// get trac message types
		List<TracMessageType> tracMessageTypes = tracMessageTypeService.selectAll();

		// get message type equal to distress notification
		TracMessageType tracMessageType = tracMessageTypes.stream().filter(x -> x.getTracMessageType().equals("DN"))
				.findFirst().orElse(null);

		if (tracMessageType == null) {
			System.out.println("Unable to find TracMessageType 'DN'. Insert to database fails.");
			return -1l;
		}

		TracMessageSent tracMessageSent = new TracMessageSent();
		tracMessageSent.setTracMessageTypeId(tracMessageType.getTracMessageTypeId());
		tracMessageSent.setDateTimeSent(new Timestamp(System.currentTimeMillis()));
		tracMessageSent.setMessageText(messageText);
		tracMessageSent.setPacketId(packetId);

		tracMessageSent.setRestResponseCode(responseCode);
		tracMessageSent.setRestResponseMessage(responseText);
		tracMessageSent.setMessageSent(messageSent);
		tracMessageSent.setEmailSent(emailSent);
		System.out.println("packet id: " + packetId);
		// log in db
		Long tracMessageSentId = tracMessageSentService.insertTracMessageSent(tracMessageSent);
		return tracMessageSentId;
	}

	public void submitDNMsgToTrac(String value, DataLoggerConfiguration config) {
		OdeTimPayload payload = jsonToJava.convertTimPayloadJsonToJava(value);

		// check if packetId is in trac message sent table
		if (isDnMsgInTrac(payload.getTim().getPacketID())) {
			// if so, return
			System.out.println("TRAC already submitted, returning");
			return;
		}

		OdeLogMetadata metadata = jsonToJava.convertTimMetadataJsonToJava(value);

		// add to db and sent to trac
		// lat and long come from the dataframes.regions.anchorPosition
		String latitude = null;
		String longitude = null;

		DataFrame[] dfs = payload.getTim().getDataframes();
		if (dfs.length > 0) {
			Region[] regs = dfs[0].getRegions();
			if (regs.length > 0) {
				latitude = regs[0].getAnchorPosition().getLatitude().toString();
				longitude = regs[0].getAnchorPosition().getLongitude().toString();
			}
		}

		// if we didn't find the anchorPosition, set it to the
		// metadata.receivedMessageDetails location information
		if (latitude == null) {
			latitude = metadata.getReceivedMessageDetails().getLocationData().getLatitude();
		}
		if (longitude == null) {
			longitude = metadata.getReceivedMessageDetails().getLocationData().getLongitude();
		}

		String descUrl = "<b>";
		descUrl += "Distress Notification Issued at ";
		descUrl += "<a href='https://www.google.com/maps/place/" + latitude + "," + longitude + "'>" + latitude + "/"
				+ longitude + "</a></b>";

		// Query parameters
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(config.getTracUrl())
				.queryParam("priority", "1").queryParam("description", descUrl)
				.queryParam("createdBy", "Connected Vehicle Emergency Notification").queryParam("source", "CV System");

		// System.out.println(builder.buildAndExpand().toUri());

		HttpHeaders responseHeaders = new HttpHeaders();
		int responseCode = -1;
		String responseText = "";
		boolean msgSent = false;
		boolean emailSent = false;
		int count = 0;
		while (!msgSent && count < 2) {
			try {
				ResponseEntity<String> response = RestTemplateProvider.GetRestTemplate().exchange(
						builder.buildAndExpand().toUri(), HttpMethod.POST,
						new HttpEntity<String>(null, responseHeaders), String.class);
				msgSent = true;
				responseCode = response.getStatusCode().value();
				responseText = response.getBody();
				System.out.println("Trac response status code: " + responseCode);
				System.out.println("Trac response message: " + responseText);
			} catch (RestClientException exception) {
				System.out.println(
						String.format("------> Failed sending message to TRAC. Exception: %s", exception.getMessage()));
				msgSent = false;
				count++;
			} catch (Exception ex) {
				// if we got here, there was an error reading the status code or body
				System.out.println("Error with parsing TRAC return value.");
				count++;
			}
		}

		if (!msgSent || responseCode != 200) {
			// either the message failed to send, or there was a failure on the Trac side
			// if the message was sent but we received a non-success code, set msgSent to
			// false for database record purposes
			if (responseCode != 200 && msgSent) {
				msgSent = !msgSent;
			}
			List<String> toAddresses = new ArrayList<String>();
			List<String> bccAddresses = new ArrayList<String>();

			// alertAddress is comma-delimited list of addresses to email
			for (String address : config.getAlertAddresses()) {
				if (address.contains("trihydro.com")) {
					// add to BCC
					bccAddresses.add(address);
				} else {
					toAddresses.add(address);
				}
			}
			if (toAddresses.size() == 0 && bccAddresses.size() > 0) {
				// switch bcc
				toAddresses = bccAddresses;
				bccAddresses = new ArrayList<String>();
			}

			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(toAddresses.toArray(new String[toAddresses.size()]));
			message.setBcc(bccAddresses.toArray(new String[bccAddresses.size()]));

			message.setSubject("Trac POST Error");
			String bodyText = "";
			if (!msgSent) {
				bodyText = "The Distress Notification failed to send to Trac. DN: ";
			} else {
				bodyText = "The Trac system failed to log the following Distress Notification: ";
			}
			bodyText += builder.buildAndExpand().toUriString();
			message.setText(bodyText);
			try {
				System.out.println("Message failed to submit to TRAC. Sending email to "
						+ String.join(",", message.getTo()) + ". BCC to " + String.join(",", message.getBcc()));
				mailProvider.getJSenderImpl(config.getMailHost(), config.getMailPort()).send(message);
				emailSent = true;
			} catch (Exception ex) {
				System.out.println(
						"------> Failed to send email, " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
				emailSent = false;
			}
		}

		logNewDistressNotification(payload.getTim().getPacketID(),
				"Distress Notification Issued at " + latitude + "/" + longitude, responseCode, responseText, msgSent,
				emailSent);
	}
}