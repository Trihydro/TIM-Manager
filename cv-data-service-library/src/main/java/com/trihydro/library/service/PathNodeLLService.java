package com.trihydro.library.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

@Component
public class PathNodeLLService extends CvDataServiceLibrary {

    public NodeXY[] getNodeLLForPath(int pathId) {
        String url = String.format("%s/path-node-ll/get-nodell-path/%d", config.getCvRestService(), pathId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<NodeXY[]> response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET, entity,
                NodeXY[].class);
        return response.getBody();
    }

}
