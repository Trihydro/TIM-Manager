package com.trihydro.library.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

public class PathNodeXYService extends CvDataServiceLibrary {

	public static Long insertPathNodeXY(Long nodeXYId, Long pathId) {
		String url = String.format("/%s/path-node-xy/add-path-nodexy/%d/%d", CVRestUrl, nodeXYId, pathId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
				Long.class);
		return response.getBody();
	}

	public static NodeXY[] GetNodeXYForPath(int pathId) {
		String url = String.format("%s/path-node-xy/get-nodexy-path/%d", CVRestUrl, pathId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<NodeXY[]> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.PUT, entity,
				NodeXY[].class);
		return response.getBody();
	}
}