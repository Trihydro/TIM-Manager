package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.WydotTim;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class MilepostService extends CvDataServiceLibrary {

	@Deprecated
	/**
	 * @deprecated Calls out to the cv-data-controller REST service to select all
	 *             mileposts within a range in one direction
	 * @param direction
	 * @param route
	 * @param fromMilepost
	 * @param toMilepost
	 * @return Mileposts found within range
	 */
	public static List<Milepost> selectMilepostRange(String direction, String route, Double fromMilepost,
			Double toMilepost) {
		String url = String.format("%s/get-milepost-range/%s/%f/%f/%s", CVRestUrl, direction, fromMilepost, toMilepost,
				route);
		ResponseEntity<Milepost[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				Milepost[].class);
		return Arrays.asList(response.getBody());
	}

	public List<Milepost> getMilepostsByStartEndPointDirection(WydotTim wydotTim) {
		String url = String.format("%s/get-milepost-start-end", CVRestUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<WydotTim> entity = new HttpEntity<WydotTim>(wydotTim, headers);
		ResponseEntity<Milepost[]> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST,
				entity, Milepost[].class);
		return Arrays.asList(response.getBody());
	}
}
