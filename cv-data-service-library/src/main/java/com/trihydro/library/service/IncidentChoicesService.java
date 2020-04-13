package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.IncidentChoice;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class IncidentChoicesService extends CvDataServiceLibrary {

	public List<IncidentChoice> selectAllIncidentActions() {
		String url = String.format("%s/incident-choice/incident-actions", config.getCvRestService());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<IncidentChoice[]> response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET,
				entity, IncidentChoice[].class);
		return Arrays.asList(response.getBody());
	}

	public List<IncidentChoice> selectAllIncidentEffects() {
		String url = String.format("%s/incident-choice/incident-effects", config.getCvRestService());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<IncidentChoice[]> response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET,
				entity, IncidentChoice[].class);
		return Arrays.asList(response.getBody());
	}

	public List<IncidentChoice> selectAllIncidentProblems() {
		String url = String.format("%s/incident-choice/incident-problems", config.getCvRestService());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<IncidentChoice[]> response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET,
				entity, IncidentChoice[].class);
		return Arrays.asList(response.getBody());
	}
}