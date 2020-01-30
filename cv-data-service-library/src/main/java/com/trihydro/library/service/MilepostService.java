package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.Milepost;

import org.springframework.http.ResponseEntity;

public class MilepostService extends CvDataServiceLibrary {
	/**
	 * Calls out to the cv-data-controller REST service to select all mileposts
	 * within a range in one direction
	 * 
	 * @param direction
	 * @param route
	 * @param fromMilepost
	 * @param toMilepost
	 * @return Mileposts found within range
	 */
	public static List<Milepost> selectMilepostRange(String direction, String route, Double fromMilepost,
			Double toMilepost) {
		String url = String.format("/%s/get-milepost-range/%s/%d/%d/%s", CVRestUrl, direction, fromMilepost, toMilepost,
				route);
		ResponseEntity<Milepost[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				Milepost[].class);
		return Arrays.asList(response.getBody());
	}
}
