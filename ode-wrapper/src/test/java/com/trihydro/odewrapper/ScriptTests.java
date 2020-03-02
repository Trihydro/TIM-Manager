package com.trihydro.odewrapper;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.odewrapper.model.TimRcList;
import com.trihydro.odewrapper.model.WydotTimRc;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Ignore
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@WebAppConfiguration
@SpringBootTest(classes = Application.class)
public class ScriptTests {

    @Test
    public void scriptTests() {

        TimRcList timRcList = new TimRcList();

        List<WydotTimRc> rcList = new ArrayList<WydotTimRc>();

        WydotTimRc tim = new WydotTimRc();

        tim.setDirection("i");
        // tim.setFromRm(370.0);
        // tim.setToRm(375.0);
        tim.setRoadCode("ARLI80EI");
        tim.setRoute("I80");
        Integer[] advisories = { 5127, 7040, 2689 };
        tim.setAdvisory(advisories);

        rcList.add(tim);
        timRcList.setTimRcList(rcList);

        Gson gson = new Gson();
        String timJson = gson.toJson(timRcList);

        RestTemplate restTemplate = new RestTemplate();

        String odeWrapperUrl = "http://cvodepp01:7777/create-update-rc-tim";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(timJson, headers);

        try {
            try {
                restTemplate.postForObject(odeWrapperUrl, entity, String.class);
            } catch (RuntimeException targetException) {
                System.out.println("exception");
            }
        } catch (RestClientException e) {

        }
    }

}
