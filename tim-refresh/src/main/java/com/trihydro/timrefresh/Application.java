package com.trihydro.timrefresh;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("Starting TIM Refresh application at " + dateFormat.format(new Date()));
        SpringApplication.run(Application.class, args);
    }
}