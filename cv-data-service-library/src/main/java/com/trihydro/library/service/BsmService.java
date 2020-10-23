package com.trihydro.library.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class BsmService extends CvDataServiceLibrary {
	/**
	 * Remove all old bsm_part2_spve, bsm_part2_suve, bsm_part2_vse, bsm_core_data
	 * records
	 * 
	 * @return
	 */
	public boolean deleteOldBsm(int retentionDays) {
		String url = String.format("%s/bsm/delete-old/%d", config.getCvRestService(), retentionDays);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<Boolean> response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.DELETE,
				entity, Boolean.class);
		return response.getBody();
	}

}