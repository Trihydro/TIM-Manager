package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.Milepost;

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

	public List<Milepost> getMilepostsByLongitudeRange(String direction, Double startLong, Double endLong,
			String commonName) {
		String url = String.format("%s/get-milepost-longitude-range/%s/%f/%f/%s", CVRestUrl, direction, startLong,
				endLong, commonName);
		ResponseEntity<Milepost[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				Milepost[].class);
		return Arrays.asList(response.getBody());
	}

	public List<Milepost> getMilepostsByLatitudeRange(String direction, Double startLat, Double endLat,
			String commonName) {
		String url = String.format("%s/get-milepost-latitude-range/%s/%f/%f/%s", CVRestUrl, direction, startLat, endLat,
				commonName);
		ResponseEntity<Milepost[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				Milepost[].class);
		return Arrays.asList(response.getBody());
	}
}
