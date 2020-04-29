package com.trihydro.library.service;

import com.trihydro.library.model.WydotOdeTravelerInformationMessage;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TimService extends CvDataServiceLibrary {
	public WydotOdeTravelerInformationMessage getTim(Long timId) {
		String url = String.format("%s/get-tim/%d", config.getCvRestService(), timId);
		ResponseEntity<WydotOdeTravelerInformationMessage> response = restTemplateProvider.GetRestTemplate()
				.getForEntity(url, WydotOdeTravelerInformationMessage.class);
		return response.getBody();
	}

	/**
	 * Remove all old tim, tim_rsu, data_frame, data_frame_itis_code, region, path,
	 * path_node_xy, node_xy records
	 * 
	 * @return
	 */
	public boolean deleteOldTim() {
		String url = String.format("%s/delete-old-tim", config.getCvRestService());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<Boolean> response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.DELETE,
				entity, Boolean.class);
		return response.getBody();
	}
}
