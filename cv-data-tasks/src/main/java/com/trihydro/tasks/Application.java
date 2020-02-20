package com.trihydro.tasks;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.tasks.actions.CleanupActiveTims;
import com.trihydro.tasks.actions.RemoveExpiredActiveTims;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	protected static DataTasksConfiguration configuration;
	private Utility utility;

	@Autowired
	public void InjectDependencies(DataTasksConfiguration configurationRhs, Utility _utility) {
		configuration = configurationRhs;
		utility=_utility;
		CvDataServiceLibrary.setCVRestUrl(configuration.getCvRestService());
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@PostConstruct
	public void run() {
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);
		scheduledExecutorService.scheduleAtFixedRate(new RemoveExpiredActiveTims(configuration, utility), 0, 4, TimeUnit.HOURS);
		scheduledExecutorService.scheduleAtFixedRate(new CleanupActiveTims(configuration, utility), 0, 4, TimeUnit.HOURS);
	}
}