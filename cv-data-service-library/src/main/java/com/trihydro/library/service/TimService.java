package com.trihydro.library.service;

import com.trihydro.library.model.TimInsertModel;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

public class TimService extends CvDataServiceLibrary {

	/**
	 * Insert a new TIM record to the database. In the case that the TIM already
	 * exists, this function returns the existing tim_id. If the TIM exists and this
	 * function is passed a satRecordId, it will update the database record to
	 * include this satRecordId
	 * 
	 * @param odeTimMetadata
	 * @param receivedMessageDetails
	 * @param j2735TravelerInformationMessage
	 * @param recordType
	 * @param logFileName
	 * @param securityResultCode
	 * @param satRecordId
	 * @param regionName
	 * @return
	 */
	public static Long insertTim(OdeMsgMetadata odeTimMetadata, ReceivedMessageDetails receivedMessageDetails,
			OdeTravelerInformationMessage j2735TravelerInformationMessage, RecordType recordType, String logFileName,
			SecurityResultCode securityResultCode, String satRecordId, String regionName) {
		String url = String.format("%s/add-tim", CVRestUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		TimInsertModel tim = new TimInsertModel();
		tim.setOdeTimMetadata(odeTimMetadata);
		tim.setReceivedMessageDetails(receivedMessageDetails);
		tim.setJ2735TravelerInformationMessage(j2735TravelerInformationMessage);
		tim.setRecordType(recordType);
		tim.setLogFileName(logFileName);
		tim.setSecurityResultCode(securityResultCode);
		tim.setSatRecordId(satRecordId);
		tim.setRegionName(regionName);
		HttpEntity<TimInsertModel> entity = new HttpEntity<TimInsertModel>(null, headers);
		ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
				Long.class);
		return response.getBody();
	}

	public static WydotOdeTravelerInformationMessage getTim(Long timId) {
		String url = String.format("%s/get-tim/%d", CVRestUrl, timId);
		ResponseEntity<WydotOdeTravelerInformationMessage> response = RestTemplateProvider.GetRestTemplate()
				.getForEntity(url, WydotOdeTravelerInformationMessage.class);
		return response.getBody();
	}
}
