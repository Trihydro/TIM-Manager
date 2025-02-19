package com.trihydro.tasks.actions;

import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
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
        utility.logWithDate("Running...", this.getClass());

        int activeTimsDeleted = 0;
        while (true) {
            try {
                // select active tims
                List<ActiveTim> activeTims = activeTimService.getExpiredActiveTims(500);
                utility.logWithDate("Retrieved a batch of " + activeTims.size() + " expired Active TIMs", this.getClass());
                if (activeTims.isEmpty()) {
                    break;
                }

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

                    utility.logWithDate("Deleting ActiveTim: { activeTimId: " + activeTim.getActiveTimId() + " }",
                            this.getClass());
                    restTemplateProvider.GetRestTemplate().exchange(configuration.getWrapperUrl() + "/delete-tim/",
                            HttpMethod.DELETE, entity, String.class);
                    activeTimsDeleted++;
                }
            } catch (Exception e) {
                utility.logWithDate("Error deleting batch of Active TIMs: ");
                e.printStackTrace();
                // don't rethrow error, or the task won't be reran until the service is
                // restarted.
            }
        }
        utility.logWithDate("Deleted a total of " + activeTimsDeleted + " Active TIMs", this.getClass());
    }
}