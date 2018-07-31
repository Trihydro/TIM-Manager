package com.trihydro.tasks;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.lang.Runnable;
import com.trihydro.library.service.ActiveTimService;
import java.sql.Connection;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

import java.util.Arrays;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import java.sql.Connection;
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
		System.out.println("testing");
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
		scheduledExecutorService.scheduleAtFixedRate(new RemoveExpiredActiveTims(), 0, 1, TimeUnit.MINUTES);	
    }

	public static class RemoveExpiredActiveTims implements Runnable {
		public void run() {

			// select active tims
			List<ActiveTim> activeTims = ActiveTimService.getExpiredActiveTims();

			// delete active tims from rsus		
				
			RestTemplate restTemplate = new RestTemplate();   
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);                
			HttpEntity<String> entity = new HttpEntity<String>(null, headers);      

			// update to send to tim type endpoint
			for (ActiveTim activeTim : activeTims) {     
				restTemplate.exchange("http://localhost:7777" + "/parking-tim/" + activeTim.getClientId(), HttpMethod.DELETE, entity, String.class);              							
			}  
			// delete active tims from sdw

			// delete expired tims from database
			ActiveTimService.deleteExpiredActiveTims();
		}
	}
}