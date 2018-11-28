package com.trihydro.odewrapper;

import com.google.gson.Gson;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimType;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.controller.WydotTimRcController;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.odewrapper.model.WydotTim;
import com.trihydro.odewrapper.model.WydotTimList;
import com.trihydro.odewrapper.model.WydotTimRc;
import com.trihydro.odewrapper.model.WydotTimRw;
import com.trihydro.odewrapper.model.WydotTravelerInputData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import us.dot.its.jpo.ode.model.OdeLogMetadata;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@WebAppConfiguration
@SpringBootTest(classes = Application.class)
public class ScriptTests {

    @Test
    public void scriptTests() {

        WydotTimList wydotTimList = new WydotTimList();

        List<WydotTimRc> rcList = new ArrayList<WydotTimRc>();

        WydotTimRc tim = new WydotTimRc();

        tim.setDirection("eastbound");
        tim.setFromRm(370.0);
        tim.setToRm(375.0);
        tim.setRoadCode("ARLI80EI");
        tim.setRoute("I80");
        Integer[] advisories = { 5127, 7040, 2689 };
        tim.setAdvisory(advisories);

        rcList.add(tim);
        wydotTimList.setTimRcList(rcList);

        Gson gson = new Gson();
        String timJson = gson.toJson(wydotTimList);

        RestTemplate restTemplate = new RestTemplate();

        String odeWrapperUrl = "http://cvodepp01:7777/create-update-rc-tim";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(timJson, headers);

        String responseStr = null;

        try {
            try {
                responseStr = restTemplate.postForObject(odeWrapperUrl, entity, String.class);
            } catch (RuntimeException targetException) {
                System.out.println("exception");
            }
        } catch (RestClientException e) {

        }
    }

}
