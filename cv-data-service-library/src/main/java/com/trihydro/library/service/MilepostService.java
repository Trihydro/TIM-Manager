package com.trihydro.library.service;

import java.util.List;

import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.MilepostBuffer;
import com.trihydro.library.model.WydotTim;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class MilepostService extends CvDataServiceLibrary {

	public List<Milepost> getMilepostsByStartEndPointDirection(WydotTim wydotTim) {
		String url = String.format("%s/get-milepost-start-end", config.getCvRestService());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<WydotTim> entity = new HttpEntity<WydotTim>(wydotTim, headers);
		ParameterizedTypeReference<List<Milepost>> responseType = new ParameterizedTypeReference<List<Milepost>>() {
		};
		ResponseEntity<List<Milepost>> response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST,
				entity, responseType);
		return response.getBody();
	}

	public List<Milepost> getMilepostsByPointWithBuffer(MilepostBuffer milepostBuffer) {
		String url = String.format("%s/get-milepost-single-point", config.getCvRestService());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<MilepostBuffer> entity = new HttpEntity<MilepostBuffer>(milepostBuffer, headers);
		ParameterizedTypeReference<List<Milepost>> responseType = new ParameterizedTypeReference<List<Milepost>>() {
		};
		ResponseEntity<List<Milepost>> response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST,
				entity, responseType);
		return response.getBody();
	}
}
