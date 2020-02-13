package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.TracMessageType;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TracMessageTypeService extends CvDataServiceLibrary {

	public List<TracMessageType> selectAll() {

		String url = String.format("/%s/trac-message-type/GetAll", CVRestUrl);
		ResponseEntity<TracMessageType[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url,
				TracMessageType[].class);
		return Arrays.asList(response.getBody());
	}

}
