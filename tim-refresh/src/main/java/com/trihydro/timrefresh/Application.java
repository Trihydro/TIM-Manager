package com.trihydro.timrefresh;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public static void main(String[] args) {
        LOG.info("Starting TIM Refresh application at {}", dateFormat.format(new Date()));
        SpringApplication.run(Application.class, args);
    }
}