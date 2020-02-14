package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.IncidentChoice;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class IncidentChoicesService extends CvDataServiceLibrary {

	public static List<IncidentChoice> selectAllIncidentActions() {
		String url = String.format("%s/incident-choice/incident-actions", CVRestUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<IncidentChoice[]> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET,
				entity, IncidentChoice[].class);
		return Arrays.asList(response.getBody());
	}

	public static List<IncidentChoice> selectAllIncidentEffects() {
		String url = String.format("%s/incident-choice/incident-effects", CVRestUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<IncidentChoice[]> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET,
				entity, IncidentChoice[].class);
		return Arrays.asList(response.getBody());
	}

	public static List<IncidentChoice> selectAllIncidentProblems() {
		String url = String.format("%s/incident-choice/incident-problems", CVRestUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<IncidentChoice[]> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET,
				entity, IncidentChoice[].class);
		return Arrays.asList(response.getBody());
	}
}