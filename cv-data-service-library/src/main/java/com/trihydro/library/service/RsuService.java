package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class RsuService extends CvDataServiceLibrary {

	public List<WydotRsu> selectAll() {
		String url = String.format("%s/rsus", config.getCvRestService());
		ResponseEntity<WydotRsu[]> response = restTemplateProvider.GetRestTemplate().getForEntity(url,
				WydotRsu[].class);
		return Arrays.asList(response.getBody());
	}

	public List<WydotRsu> selectRsusByRoute(String route) {
		String url = String.format("%s/rsus-by-route/%s", config.getCvRestService(), route);
		ResponseEntity<WydotRsu[]> response = restTemplateProvider.GetRestTemplate().getForEntity(url,
				WydotRsu[].class);
		return Arrays.asList(response.getBody());
	}

	public List<WydotRsuTim> getFullRsusTimIsOn(Long timId) {
		String url = String.format("%s/rsus-for-tim/%d", config.getCvRestService(), timId);
		ResponseEntity<WydotRsuTim[]> response = restTemplateProvider.GetRestTemplate().getForEntity(url,
				WydotRsuTim[].class);
		return Arrays.asList(response.getBody());
	}
}