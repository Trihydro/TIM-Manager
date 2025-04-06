package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.tasks.config.DataTasksConfiguration;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CleanupActiveTims implements Runnable {
    private DataTasksConfiguration configuration;
    private Utility utility;
    private ActiveTimService activeTimService;
    private RestTemplateProvider restTemplateProvider;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration _configuration, Utility _utility,
            ActiveTimService _activeTimService, RestTemplateProvider _restTemplateProvider) {
        configuration = _configuration;
        utility = _utility;
        activeTimService = _activeTimService;
        restTemplateProvider = _restTemplateProvider;
    }

    public void run() {
        log.info("Running...");

        try {
            List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
            List<ActiveTim> tmp = null;

            // select active tims missing ITIS codes
            tmp = activeTimService.getActiveTimsMissingItisCodes();
            if (tmp.size() > 0) {
                log.info("Found {} Active TIMs missing ITIS Codes", tmp.size());
                activeTims.addAll(tmp);
            }

            // add active tims that weren't sent to the SDX or any RSUs
            tmp = activeTimService.getActiveTimsNotSent();
            if (tmp.size() > 0) {
                log.info("Found {} Active TIMs that weren't distributed", tmp.size());
                activeTims.addAll(tmp);
            }

            if (activeTims.size() == 0) {
                log.info("Found 0 Active TIMs");
            }

            // delete from rsus and the SDX
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = null;
            String activeTimJson;
            Gson gson = new Gson();

            // send to tim type endpoint to delete from RSUs and SDWs
            for (ActiveTim activeTim : activeTims) {

                activeTimJson = gson.toJson(activeTim);
                entity = new HttpEntity<String>(activeTimJson, headers);

                String msg = "CleanupActiveTims - Deleting ActiveTim: { activeTimId: " + activeTim.getActiveTimId() + " }";
                log.info(msg);
                restTemplateProvider.GetRestTemplate().exchange(configuration.getWrapperUrl() + "/delete-tim/",
                        HttpMethod.DELETE, entity, String.class);
            }
        } catch (Exception e) {
            log.error("Exception", e);
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }
    }
}