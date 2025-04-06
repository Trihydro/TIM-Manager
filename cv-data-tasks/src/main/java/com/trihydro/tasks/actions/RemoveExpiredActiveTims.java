package com.trihydro.tasks.actions;

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
public class RemoveExpiredActiveTims implements Runnable {
    private DataTasksConfiguration configuration;
    private Utility utility;
    private ActiveTimService activeTimService;
    private RestTemplateProvider restTemplateProvider;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration configuration, Utility _utility,
            ActiveTimService _activeTimService, RestTemplateProvider _restTemplateProvider) {
        this.configuration = configuration;
        utility = _utility;
        activeTimService = _activeTimService;
        restTemplateProvider = _restTemplateProvider;
    }

    public void run() {
        log.info("Running...");

        try {
            // select active tims
            List<ActiveTim> activeTims = activeTimService.getExpiredActiveTims();
            log.info("Found {} expired Active TIMs", activeTims.size());

            // delete active tims from rsus
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = null;
            String activeTimJson;
            Gson gson = new Gson();

            // send to tim type endpoint to delete from RSUs and SDWs
            for (ActiveTim activeTim : activeTims) {

                activeTimJson = gson.toJson(activeTim);
                entity = new HttpEntity<String>(activeTimJson, headers);

                String msg = "Deleting ActiveTim: { activeTimId: " + activeTim.getActiveTimId() + " }";
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