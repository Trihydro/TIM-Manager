package com.trihydro.tasks.actions;

import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.tasks.config.DataTasksConfiguration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
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

    /**
     * This method is called by the task scheduler to remove expired Active TIMs in batches.
     *
     * The rationale for processing batches of expired Active TIMs is to avoid connection timeouts
     * when deleting a large number of Active TIMs. If we tried to delete all expired Active TIMs
     * in one batch, and it took longer than the configured connection timeout, the task would fail
     * and the Active TIMs would not be deleted.
     */
    public void run() {
        log.info("Running...");

        int batchSize = 500;
        int maxBatchCount = 50;
        int batchCount = 0;
        while (true) {
            try {
                // select active tims
                List<ActiveTim> activeTims = activeTimService.getExpiredActiveTims(batchSize);
                log.info("Retrieved a batch of {} expired Active TIMs", activeTims.size());
                if (activeTims.isEmpty()) {
                    break;
                }

                // delete active tims from rsus and sdx
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = null;
                String activeTimJson;
                Gson gson = new Gson();

                // send to tim type endpoint to delete from RSUs and SDX
                for (ActiveTim activeTim : activeTims) {

                    activeTimJson = gson.toJson(activeTim);
                    entity = new HttpEntity<String>(activeTimJson, headers);

                    log.info("Attempting to delete ActiveTim: { activeTimId: {} }", activeTim.getActiveTimId());
                    restTemplateProvider.GetRestTemplate()
                        .exchange(configuration.getWrapperUrl() + "/delete-tim/",
                            HttpMethod.DELETE, entity, String.class);
                }
            } catch (ResourceAccessException e) {
                log.error("Error accessing resource. This indicates that the ODE Wrapper or CV Data Controller is not reachable.", e);
                // the error should not be rethrown, or else the task will not run until the service is restarted
            } catch (Exception e) {
                log.error("Unexpected error occurred while processing expired Active TIMs", e);
                // the error should not be rethrown, or else the task will not run until the service is restarted
            }
            batchCount++;
            if (batchCount >= maxBatchCount) {
                log.warn("Maximum batches reached. No more batches will be processed until the next run. This indicates either A) repeated failures to delete expired Active TIMs or B) a large number of expired Active TIMs (more than {})", maxBatchCount * batchSize);
                break;
            }
        }
        log.info("{} attempts were made to process batches of expired Active TIMs", batchCount);
    }
}