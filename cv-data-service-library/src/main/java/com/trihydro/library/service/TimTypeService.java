package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.TimType;

import org.springframework.http.ResponseEntity;

public class TimTypeService extends CvDataServiceLibrary {

	public static List<TimType> selectAll() {
		String url = String.format("%s/tim-type/tim-types", CVRestUrl);
		ResponseEntity<TimType[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url, TimType[].class);
		return Arrays.asList(response.getBody());
	}
}
