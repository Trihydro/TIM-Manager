package com.trihydro.library.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class DataFrameItisCodeService extends CvDataServiceLibrary {
	public static Long insertDataFrameItisCode(Long dataFrameId, String itis) {
		String url = String.format("%s/data-frame-itis-code/add-data-frame-itis-code/%d/%s", CVRestUrl, dataFrameId, itis);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
				Long.class);
		return response.getBody();
	}
}