package com.trihydro.library.service;

import com.trihydro.library.model.ActiveTimHolding;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ActiveTimHoldingService extends CvDataServiceLibrary {

    public long insertActiveTimHolding(ActiveTimHolding activeTimHolding){
        String url = String.format("%s/active-tim-holding/add", CVRestUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<ActiveTimHolding> entity = new HttpEntity<ActiveTimHolding>(activeTimHolding, headers);
		ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
				Long.class);
		return response.getBody();
    }
}