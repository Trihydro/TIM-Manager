package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.BsmCoreDataPartition;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class UtilityService extends CvDataServiceLibrary {
    public List<BsmCoreDataPartition> getBsmCoreDataPartitions() {
        String url = String.format("%s/utility/bsm-core-data-partitions", config.getCvRestService());
        ResponseEntity<BsmCoreDataPartition[]> response = restTemplateProvider.GetRestTemplate().getForEntity(url,
                BsmCoreDataPartition[].class);

        return Arrays.asList(response.getBody());
    }

    public boolean dropBsmPartitions(List<String> partitionNames) {
        String url = String.format("%s/utility/drop-bsm-partitions", config.getCvRestService());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<String>> entity = new HttpEntity<List<String>>(partitionNames, headers);
        ResponseEntity<Boolean> response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
                Boolean.class);

        return response.getBody();
    }
}