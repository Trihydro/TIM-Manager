package com.trihydro.library.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.OdeProps;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotTravelerInputData;

import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;
import us.dot.its.jpo.ode.plugin.SnmpProtocol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class OdeService {

    private Gson gson = new Gson();
    private RestTemplateProvider restTemplateProvider;
    private Utility utility;
    private OdeProps odeProps;

    @Autowired
    public void InjectDependencies(Utility _utility, RestTemplateProvider _restTemplateProvider, OdeProps _odeProps) {
        utility = _utility;
        restTemplateProvider = _restTemplateProvider;
        odeProps = _odeProps;
    }

    public String sendNewTimToRsu(WydotTravelerInputData timToSend) {
        utility.logWithDate("Sending the following new TIM to ODE for processing: " + gson.toJson(timToSend));
        String exMsg = "";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WydotTravelerInputData> entity = new HttpEntity<WydotTravelerInputData>(timToSend, headers);
        ResponseEntity<String> response = restTemplateProvider.GetRestTemplate_NoErrors()
                .exchange(odeProps.getOdeUrl() + "/tim", HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().series() != HttpStatus.Series.SUCCESSFUL) {
            exMsg = "Failed to send new TIM to RSU: " + response.getBody();
            utility.logWithDate(exMsg);
        }
        return exMsg;
    }

    public String sendNewTimToSdw(WydotTravelerInputData timToSend, String recordId, List<Milepost> mps) {
        String exMsg = "";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WydotTravelerInputData> entity = new HttpEntity<WydotTravelerInputData>(timToSend, headers);
        ResponseEntity<String> response = restTemplateProvider.GetRestTemplate_NoErrors()
                .exchange(odeProps.getOdeUrl() + "/tim", HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().series() != HttpStatus.Series.SUCCESSFUL) {
            exMsg = "Failed to send new TIM to SDX: " + response.getBody();
            utility.logWithDate(exMsg);
        }
        return exMsg;
    }

    public String updateTimOnSdw(WydotTravelerInputData timToSend) {

        String exMsg = "";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WydotTravelerInputData> entity = new HttpEntity<WydotTravelerInputData>(timToSend, headers);
        ResponseEntity<String> response = restTemplateProvider.GetRestTemplate_NoErrors()
                .exchange(odeProps.getOdeUrl() + "/tim", HttpMethod.PUT, entity, String.class);
        if (response.getStatusCode().series() != HttpStatus.Series.SUCCESSFUL) {
            exMsg = "Failed to update TIM on SDX: " + response.getBody();
            utility.logWithDate(exMsg);
        }
        return exMsg;
    }

    public Integer findFirstAvailableIndexWithRsuIndex(List<Integer> indicies) {
        if (indicies == null) {
            return null;
        }

        for (int i = 2; i < 100; i++) {
            if (!indicies.contains(i)) {
                return i;
            }
        }

        return null;
    }

    public TimQuery submitTimQuery(WydotRsu rsu, int counter) {

        // stop if this fails twice
        if (counter == 2)
            return null;

        // tim query to ODE
        var odeRsu = new RSU();
        odeRsu.setSnmpProtocol(SnmpProtocol.NTCIP1218);
        odeRsu.setRsuTarget(rsu.getRsuTarget());
        // rsuUsername, rsuPassword will take ODE defaults.
        odeRsu.setRsuRetries(3);
        odeRsu.setRsuTimeout(5000);
        String rsuJson = gson.toJson(odeRsu);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(rsuJson, headers);

        String responseStr = null;

        try {
            responseStr = restTemplateProvider.GetRestTemplate().postForObject(odeProps.getOdeUrl() + "/tim/query",
                    entity, String.class);
        } catch (RestClientException e) {
            return submitTimQuery(rsu, counter + 1);
        }

        String[] items = responseStr.replaceAll("\\\"", "").replaceAll("\\:", "").replaceAll("indicies_set", "")
                .replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\\[", "").replaceAll(" ", "")
                .replaceAll("\\]", "").replaceAll("\\s", "").split(",");

        List<Integer> results = new ArrayList<Integer>();

        for (int i = 0; i < items.length; i++) {
            try {
                results.add(Integer.parseInt(items[i]));
            } catch (NumberFormatException nfe) {
                // NOTE: write something here if you need to recover from formatting errors
            }
        }

        Collections.sort(results);

        TimQuery timQuery = new TimQuery();
        timQuery.setIndicies_set(results);
        return timQuery;
    }

    public String deleteTimFromRsu(RSU rsu, Integer index) {
        String exMsg = "";

        String rsuJson = gson.toJson(rsu);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(rsuJson, headers);

        utility.logWithDate("Deleting TIM on index " + index.toString() + " from rsu " + rsu.getRsuTarget());

        try {
            // ODE response is misleading due to poor interpretation of SNMP results. If we
            // establish a connection with the ODE to execute this request, we'll assume
            // the deletion has at least been attempted.
            restTemplateProvider.GetRestTemplate_NoErrors().exchange(
                    odeProps.getOdeUrl() + "/tim?index=" + index.toString(), HttpMethod.DELETE, entity, String.class);
        } catch (RestClientException ex) {
            exMsg = "Failed to contact ODE to delete message from index " + index + " on RSU " + rsu.getRsuTarget();
            utility.logWithDate(exMsg);
        }

        return exMsg;
    }
}