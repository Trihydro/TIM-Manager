package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.TracMessageSent;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TracMessageSentService extends CvDataServiceLibrary {

	public List<String> selectPacketIds() {
		String url = String.format("/%s/trac-message/packet-ids", CVRestUrl);
		ResponseEntity<String[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url, String[].class);
		return Arrays.asList(response.getBody());
	}

	public Long insertTracMessageSent(TracMessageSent tracMessageSent) {
		String url = String.format("/%s/trac-message/add-trac-message-sent", CVRestUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<TracMessageSent> entity = new HttpEntity<TracMessageSent>(tracMessageSent, headers);
		ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
				Long.class);
		return response.getBody();
	}
}
