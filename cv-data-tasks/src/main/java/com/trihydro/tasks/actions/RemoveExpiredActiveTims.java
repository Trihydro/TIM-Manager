package com.trihydro.tasks.actions;

import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.tasks.config.BasicConfiguration;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

public class RemoveExpiredActiveTims implements Runnable {
    private BasicConfiguration configuration;

    public RemoveExpiredActiveTims(BasicConfiguration configuration) {
        this.configuration = configuration;
    }

    public void run() {
        Utility.logWithDate("RemoveExpiredActiveTims - Running...");

        try {
            // select active tims
            List<ActiveTim> activeTims = ActiveTimService.getExpiredActiveTims();
            Utility.logWithDate("RemoveExpiredActiveTims - Found " + activeTims.size() + " expired Active TIMs");

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

                Utility.logWithDate("RemoveExpiredActiveTims - Deleting ActiveTim: { activeTimId: "
                        + activeTim.getActiveTimId() + " }");
                RestTemplateProvider.GetRestTemplate().exchange(configuration.getWrapperUrl() + "/delete-tim/",
                        HttpMethod.DELETE, entity, String.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }
    }
}