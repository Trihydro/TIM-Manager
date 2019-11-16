package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.tasks.config.BasicConfiguration;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

public class CleanupActiveTims implements Runnable {
    private BasicConfiguration configuration;

    public CleanupActiveTims(BasicConfiguration configuration) {
        this.configuration = configuration;
    }
    
    public void run() {
        try {
            List<ActiveTim> activeTims = new ArrayList<ActiveTim>();

            // select active tims missing ITIS codes
            activeTims.addAll(ActiveTimService.getActiveTimsMissingItisCodes());
            // add active tims that weren't sent to the SDX or any RSUs
            activeTims.addAll(ActiveTimService.getActiveTimsNotSent());

            // delete from rsus and the SDX
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = null;
            String activeTimJson;
            Gson gson = new Gson();

            // send to tim type endpoint to delete from RSUs and SDWs
            for (ActiveTim activeTim : activeTims) {

                activeTimJson = gson.toJson(activeTim);
                entity = new HttpEntity<String>(activeTimJson, headers);

                restTemplate.exchange(configuration.getWrapperUrl() + "/delete-tim/", HttpMethod.DELETE, entity,
                        String.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // and re-throw it so that the Executor also gets this error
            throw new RuntimeException(e);
        }
    }
}