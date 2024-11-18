package com.trihydro.library.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DataFrameService extends CvDataServiceLibrary {

	/**
	 * Calls out to cv-data-controller REST service to fetch ITIS codes associated
	 * with a given DataFrame id
	 * 
	 * @param dataFrameId
	 * @return String array of all ITIS codes associated with dataFrameId
	 */
	public String[] getItisCodesForDataFrameId(Integer dataFrameId) {
		String url = String.format("%s/data-frame/itis-for-data-frame/%d", config.getCvRestService(), dataFrameId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<String[]> response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET, entity,
				String[].class);
		return response.getBody();
	}

}
