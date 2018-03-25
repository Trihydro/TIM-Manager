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

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
	}

	public Application() {
		System.out.println("testing");
				
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
		scheduledExecutorService.scheduleAtFixedRate(new RemoveExpiredActiveTims(), 0, 1, TimeUnit.HOURS);	
    }

	public static class RemoveExpiredActiveTims implements Runnable {
		public void run() {
			ActiveTimService.deleteExpiredActiveTims();
		}
	}

}