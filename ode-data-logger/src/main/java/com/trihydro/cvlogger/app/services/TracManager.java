package com.trihydro.cvlogger.app.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;

import com.trihydro.library.service.TracMessageSentService;
import com.trihydro.library.service.TracMessageTypeService;
import com.trihydro.cvlogger.app.converters.JsonToJavaConverter;
import com.trihydro.library.model.ConfigProperties;
import com.trihydro.library.model.TracMessageSent;
import com.trihydro.library.model.TracMessageType;

@Component
public class TracManager {
	@Autowired
	private JavaMailSender jms;

	public TracMessageSent isDnMsgInTrac(String packetId) {

		List<TracMessageSent> tracMessagesSent = TracMessageSentService.selectAll();

		TracMessageSent tracMessageSent = tracMessagesSent.stream().filter(x -> x.getPacketId().equals(packetId))
				.findFirst().orElse(null);

		return tracMessageSent;
	}

	public Long logNewDistressNotification(String packetId, String messageText, int responseCode, String responseText,
			boolean messageSent, boolean emailSent) {

		// get trac message types
		List<TracMessageType> tracMessageTypes = TracMessageTypeService.selectAll();

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
		Long tracMessageSentId = TracMessageSentService.insertTracMessageSent(tracMessageSent);
		return tracMessageSentId;
	}

	public void submitDNMsgToTrac(String value, ConfigProperties config) {
		OdeLogMetadata metadata = JsonToJavaConverter.convertTimMetadataJsonToJava(value);
		OdeTimPayload payload = JsonToJavaConverter.convertTimPayloadJsonToJava(value);

		// check if packetId is in trac message sent table
		if (isDnMsgInTrac(payload.getTim().getPacketID()) != null) {
			// if so, return
			System.out.println("TRAC already submitted, returning");
			return;
		}

		// if not, add to db and sent to trac
		// RestTemplate restTemplate = new RestTemplate();
		// String url = "http://gisd01:8070/trac/newtask.text";

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
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(config.getGetTrackUrl())
				.queryParam("priority", "1").queryParam("description", descUrl)
				.queryParam("createdBy", "Connected Vehicle Emergency Notification").queryParam("source", "CV System");

		System.out.println(builder.buildAndExpand().toUri());

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
						String.format("Failed sending message to TRAC. Exception: %s", exception.getMessage()));
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
						+ String.join(",", message.getTo()) + ". BCC to " + message.getBcc());
				jms.send(message);
				emailSent = true;
			} catch (Exception ex) {
				System.out.println("Failed to send email: " + ex.getMessage());
				emailSent = false;
			}
		}

		logNewDistressNotification(payload.getTim().getPacketID(),
				"Distress Notification Issued at " + latitude + "/" + longitude, responseCode, responseText, msgSent,
				emailSent);
	}
}