package com.trihydro.library.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

public class NodeXYService extends CvDataServiceLibrary {

    public static Long insertNodeXY(OdeTravelerInformationMessage.NodeXY nodeXY) {
        String url = String.format("/%s/nodexy/add-nodexy", CVRestUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OdeTravelerInformationMessage.NodeXY> entity = new HttpEntity<OdeTravelerInformationMessage.NodeXY>(
                nodeXY, headers);
        ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
                Long.class);
        return response.getBody();
    }
}
