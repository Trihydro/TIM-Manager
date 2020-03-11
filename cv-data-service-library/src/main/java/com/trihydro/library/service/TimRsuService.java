package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.TimRsu;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class TimRsuService extends CvDataServiceLibrary {

	public static Long insertTimRsu(Long timId, Integer rsuId, Integer rsuIndex) {
		String url = String.format("%s/tim-rsu/add-tim-rsu/%d/%d/%d", CVRestUrl, timId, rsuId, rsuIndex);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
				Long.class);
		return response.getBody();
	}

	public static List<TimRsu> getTimRsusByTimId(Long timId) {
		// tim-id
		String url = String.format("%s/tim-rsu/tim-id/%d", CVRestUrl, timId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<TimRsu[]> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET, entity,
				TimRsu[].class);
		return Arrays.asList(response.getBody());
	}

	public static TimRsu getTimRsu(Long timId, Integer rsuId) {
		String url = String.format("%s/tim-rsu/tim-rsu/%d/%d", CVRestUrl, timId, rsuId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<TimRsu> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET, entity,
				TimRsu.class);
		return response.getBody();
	}
}
