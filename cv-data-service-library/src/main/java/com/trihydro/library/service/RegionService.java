package com.trihydro.library.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;

public class RegionService extends CvDataServiceLibrary {

	public static Long insertRegion(Long dataFrameId, Long pathId, Region region) {
		String url = String.format("/%s/region/add-region/%d/%d", CVRestUrl, dataFrameId, pathId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Region> entity = new HttpEntity<Region>(region, headers);
		ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.PUT, entity,
				Long.class);
		return response.getBody();
	}

	public static Boolean updateRegionName(Long regionId, String name) {
		String url = String.format("/%s/region/update-region-name/%d/%s", CVRestUrl, regionId, name);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<Boolean> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.PUT, entity,
				Boolean.class);
		return response.getBody();
	}

}
