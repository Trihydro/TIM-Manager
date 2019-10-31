package com.trihydro.tasks;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.lang.Runnable;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.CvDataServiceLibrary;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;

import java.util.List;

import com.trihydro.tasks.config.BasicConfiguration;
import com.google.gson.Gson;
import com.trihydro.library.model.ActiveTim;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

@SpringBootApplication
public class Application {

	protected static BasicConfiguration configuration;

	@Autowired
	public void setConfiguration(BasicConfiguration configurationRhs) {
		configuration = configurationRhs;
		CvDataServiceLibrary.setConfig(configuration);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	public Application() {
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
		scheduledExecutorService.scheduleAtFixedRate(new RemoveExpiredActiveTims(), 1, 5, TimeUnit.MINUTES);
	}

	public static class RemoveExpiredActiveTims implements Runnable {
		public void run() {
			try {
				// select active tims
				List<ActiveTim> activeTims = ActiveTimService.getExpiredActiveTims();

				// delete active tims from rsus

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
				// and re throw it so that the Executor also gets this error so that it can do
				// what it would
				// usually do
				throw new RuntimeException(e);
			}
		}
	}
}