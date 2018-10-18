package com.trihydro.tasks;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.lang.Runnable;
import com.trihydro.library.service.ActiveTimService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

import java.util.List;
import com.trihydro.library.model.ActiveTim;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	public Application() {
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
		scheduledExecutorService.scheduleAtFixedRate(new RemoveExpiredActiveTims(), 0, 1, TimeUnit.HOURS);
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
				HttpEntity<String> entity = new HttpEntity<String>(null, headers);

				// send to tim type endpoint to delete from RSUs and SDWs
				for (ActiveTim activeTim : activeTims) {
					restTemplate.exchange("http://cvodedp01:7777" + "/parking-tim/" + activeTim.getClientId(),
							HttpMethod.DELETE, entity, String.class);
				}

				// delete expired tims from database
				ActiveTimService.deleteExpiredActiveTims();
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