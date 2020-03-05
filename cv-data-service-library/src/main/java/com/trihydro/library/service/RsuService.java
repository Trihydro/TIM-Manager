package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class RsuService extends CvDataServiceLibrary {

	public static List<WydotRsu> selectAll() {
		String url = String.format("%s/rsus", CVRestUrl);
		ResponseEntity<WydotRsu[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				WydotRsu[].class);
		return Arrays.asList(response.getBody());
	}

	public static List<WydotRsu> selectRsusByRoute(String route) {
		String url = String.format("%s/rsus-by-route/%s", CVRestUrl, route);
		ResponseEntity<WydotRsu[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				WydotRsu[].class);
		return Arrays.asList(response.getBody());
	}

	public static List<WydotRsuTim> getFullRsusTimIsOn(Long timId) {
		String url = String.format("%s/rsus-for-tim/%d", CVRestUrl, timId);
		ResponseEntity<WydotRsuTim[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				WydotRsuTim[].class);
		return Arrays.asList(response.getBody());
	}
}