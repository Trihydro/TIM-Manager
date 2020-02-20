package com.trihydro.library.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;

public class DataFrameService extends CvDataServiceLibrary {

	/**
	 * Calls out to the cv-data-controller REST service to insert a new dataFrame record
	 * @param timID
	 * @param dFrame
	 * @return
	 */
	public static Long insertDataFrame(Long timID, DataFrame dFrame) {
		String url = String.format("%s/data-frame/add-data-frame/%d", CVRestUrl, timID);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<DataFrame> entity = new HttpEntity<DataFrame>(dFrame, headers);
		ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
				Long.class);
		return response.getBody();
	}

	/**
	 * Calls out to cv-data-controller REST service to fetch ITIS codes associated
	 * with a given DataFrame id
	 * 
	 * @param dataFrameId
	 * @return String array of all ITIS codes associated with dataFrameId
	 */
	public static String[] getItisCodesForDataFrameId(Integer dataFrameId) {
		String url = String.format("%s/data-frame/itis-for-data-frame/%d", CVRestUrl, dataFrameId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<String[]> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET, entity,
				String[].class);
		return response.getBody();
	}

}
