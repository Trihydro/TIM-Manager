package com.trihydro.library.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.SDXDecodeRequest;
import com.trihydro.library.model.SDXDecodeResponse;
import com.trihydro.library.model.SDXQuery;
import com.trihydro.library.model.SdwProps;
import com.trihydro.library.model.SemiDialogID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Component
public class SdwService {
    private static final Logger LOG = LoggerFactory.getLogger(SdwService.class);
    public Gson gson = new Gson();
    private SdwProps configProperties;
    private Utility utility;
    private RestTemplateProvider restTemplateProvider;

    @Autowired
    public void InjectDependencies(SdwProps _config, Utility _utility, RestTemplateProvider _restTemplateProvider) {
        configProperties = _config;
        utility = _utility;
        restTemplateProvider = _restTemplateProvider;
    }

    /**
     * Fetches messages deposited into the SDX, by the ODE User (identified by
     * apikey).
     * 
     * @param type Type of message to retrieve
     */
    public List<AdvisorySituationDataDeposit> getMsgsForOdeUser(SemiDialogID type) throws RestClientException {
        List<AdvisorySituationDataDeposit> results = null;

        String url = String.format("%s/api/deposited-by-me/%d", configProperties.getSdwRestUrl(), type.getValue());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("apikey", configProperties.getSdwApiKey());

        HttpEntity<SDXQuery> entity = new HttpEntity<SDXQuery>(null, headers);
        try {
            ResponseEntity<AdvisorySituationDataDeposit[]> response = restTemplateProvider.GetRestTemplate()
                    .exchange(url, HttpMethod.GET, entity, AdvisorySituationDataDeposit[].class);

            results = Arrays.asList(response.getBody());
        } catch (RestClientException ex) {
            LOG.info("An exception occurred while attempting to get messages from SDX: {}", ex.getMessage());
            LOG.info("Is the SDX API key valid?");
            LOG.error("Exception", ex);
        }

        return results;
    }

    public List<Integer> getItisCodesFromAdvisoryMessage(String advisoryMessage) throws IllegalArgumentException {
        if (advisoryMessage == null) {
            throw new IllegalArgumentException("advisoryMessage cannot be null");
        }

        int idx = advisoryMessage.indexOf("001F");
        if (idx < 0) {
            throw new IllegalArgumentException("Cannot determine start of MessageFrame");
        }

        List<Integer> results = new ArrayList<Integer>();
        SDXDecodeResponse decodeResponse = null;

        // Build request
        String url = String.format("%s/api/decode", configProperties.getSdwRestUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("apikey", configProperties.getSdwApiKey());

        SDXDecodeRequest request = new SDXDecodeRequest();
        request.setEncodeType("hex");
        request.setMessageType("MessageFrame");
        request.setEncodedMsg(advisoryMessage.substring(idx));

        // Execute request
        try {
            HttpEntity<SDXDecodeRequest> entity = new HttpEntity<SDXDecodeRequest>(request, headers);
            ResponseEntity<SDXDecodeResponse> response = restTemplateProvider.GetRestTemplate().exchange(url,
                    HttpMethod.POST, entity, SDXDecodeResponse.class);

            decodeResponse = response.getBody();
        } catch (RestClientException ex) {
            LOG.info("An exception occurred while attempting to decode message: {}", ex.getMessage());
            LOG.info("Is the SDX API key valid?");
            LOG.error("Exception", ex);
            return null;
        }

        // Process request (convert decodedMessage into an array of ITIS codes)
        Pattern p = Pattern.compile("(<itis>)([0-9]*)(</itis>)");
        Matcher m = p.matcher(decodeResponse.getDecodedMessage());

        while (m.find()) {
            String itisCode = m.group(2);

            try {
                results.add(Integer.parseInt(itisCode));
            } catch (NumberFormatException ex) {
                LOG.error("Exception", ex);
            }
        }

        return results;
    }

    /**
     * Returns a pseudo-random 4 byte hex value representing recordId. This 4 byte
     * limitation comes from asn1_codec SEMI_v2.3.0_070616.asn found at
     * https://github.com/usdot-jpo-ode/asn1_codec/blob/master/asn1c_combined/SEMI_v2.3.0_070616.asn
     * 
     * @return
     */
    public String getNewRecordId() {
        String hexChars = "ABCDEF1234567890";
        StringBuilder hexStrB = new StringBuilder();
        Random rnd = new Random();
        while (hexStrB.length() < 8) { // length of the random string.
            int index = (int) (rnd.nextFloat() * hexChars.length());
            hexStrB.append(hexChars.charAt(index));
        }
        String hexStr = hexStrB.toString();
        return hexStr;
    }

    public HashMap<Integer, Boolean> deleteSdxDataBySatRecordId(List<String> satRecordIds) {
        HashMap<Integer, Boolean> results = null;
        if (satRecordIds == null || satRecordIds.size() == 0 || configProperties.getSdwApiKey() == null) {
            if (configProperties.getSdwApiKey() == null) {
                LOG.info("Attempting to delete satellite records failed due to null apiKey");
            } else {
                LOG.info("Attempting to delete satellite records failed due to no satRecordIds passed in");
            }
            return results;
        }

        List<Integer> satRecordInts = satRecordIds.stream().map(x -> Integer.parseUnsignedInt(x, 16))
                .collect(Collectors.toList());

        String url = getBaseUrlString("api/delete-multiple-by-recordid");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("apikey", configProperties.getSdwApiKey());
        HttpEntity<List<Integer>> entity = new HttpEntity<List<Integer>>(satRecordInts, headers);
        ParameterizedTypeReference<HashMap<Integer, Boolean>> responseType = new ParameterizedTypeReference<HashMap<Integer, Boolean>>() {
        };
        ResponseEntity<HashMap<Integer, Boolean>> response;
        try {
            response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.DELETE, entity, responseType);
        } catch (HttpClientErrorException ex) {
            LOG.info("An exception occurred while attempting to delete satellite records: {}", ex.getMessage());
            LOG.info("Is the SDX API key valid?");
            LOG.error("Exception", ex);
            response = new ResponseEntity<HashMap<Integer, Boolean>>(ex.getStatusCode());
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            LOG.info("Failed to call delete-multiple-by-id on SDX api");
        }
        return response.getBody();
    }

    public HashMap<Integer, Boolean> deleteSdxDataByRecordIdIntegers(List<Integer> satRecordInts) {
        HashMap<Integer, Boolean> results = null;
        if (satRecordInts == null || satRecordInts.size() == 0 || configProperties.getSdwApiKey() == null) {
            if (configProperties.getSdwApiKey() == null) {
                LOG.info("Attempting to delete satellite records failed due to null apiKey");
            } else {
                LOG.info("Attempting to delete satellite records failed due to no satRecordIds passed in");
            }
            return results;
        }

        String url = getBaseUrlString("api/delete-multiple-by-recordid");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("apikey", configProperties.getSdwApiKey());
        HttpEntity<List<Integer>> entity = new HttpEntity<List<Integer>>(satRecordInts, headers);
        ParameterizedTypeReference<HashMap<Integer, Boolean>> responseType = new ParameterizedTypeReference<HashMap<Integer, Boolean>>() {
        };
        ResponseEntity<HashMap<Integer, Boolean>> response;
        try {
            response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.DELETE, entity, responseType);
        } catch (HttpClientErrorException ex) {
            LOG.info("An exception occurred while attempting to delete satellite records: {}", ex.getMessage());
            LOG.info("Is the SDX API key valid?");
            LOG.error("Exception", ex);
            response = new ResponseEntity<HashMap<Integer, Boolean>>(ex.getStatusCode());
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            LOG.info("Failed to call delete-multiple-by-id on SDX api");
        }
        return response.getBody();
    }

    private String getBaseUrlString(String end) {
        String baseUrl = configProperties.getSdwRestUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        baseUrl += end;
        return baseUrl;
    }
}