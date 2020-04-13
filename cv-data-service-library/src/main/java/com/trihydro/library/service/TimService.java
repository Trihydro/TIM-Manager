package com.trihydro.library.service;

import com.trihydro.library.model.WydotOdeTravelerInformationMessage;

import org.springframework.http.ResponseEntity;

public class TimService extends CvDataServiceLibrary {
	public WydotOdeTravelerInformationMessage getTim(Long timId) {
		String url = String.format("%s/get-tim/%d", config.getCvRestService(), timId);
		ResponseEntity<WydotOdeTravelerInformationMessage> response = restTemplateProvider.GetRestTemplate()
				.getForEntity(url, WydotOdeTravelerInformationMessage.class);
		return response.getBody();
	}
}
