package com.trihydro.tasks;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.lang.Runnable;
import com.trihydro.library.service.tim.ActiveTimLogger;
import com.trihydro.tasks.helpers.DBUtility;
import java.sql.Connection;

public class CvTaskRunner {

	private static DBUtility dbUtility; 
	private static Connection connection;

	public static void main( String[] args ) {

		dbUtility = new DBUtility();
		connection = dbUtility.getConnection();

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

		scheduledExecutorService.scheduleAtFixedRate(new RemoveExpiredActiveTims(), 0, 10, TimeUnit.SECONDS);	
		//scheduledExecutorService.scheduleAtFixedRate(new MyRunnableTask2(), 0, 10, TimeUnit.SECONDS);	
	}

	public static class RemoveExpiredActiveTims implements Runnable {
		public void run() {
			System.out.println("Test!");
			ActiveTimLogger.deleteExpiredActiveTims(dbUtility.getConnection());
		}
	}

	public static class MyRunnableTask2 implements Runnable {
		public void run() {
			System.out.println("Test 2");
		}
	}
}