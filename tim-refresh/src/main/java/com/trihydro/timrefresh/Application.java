package com.trihydro.timrefresh;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.timrefresh.config.BasicConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    protected static BasicConfiguration configuration;

    @Autowired
    public void setConfiguration(BasicConfiguration configurationRhs) {
        System.out.println("Setting configuration");
        configuration = configurationRhs;
        CvDataServiceLibrary.setConfig(configuration);
    }

    public static void main(String[] args) {
        System.out.println("Starting TIM Refresh application at " + dateFormat.format(new Date()));
        SpringApplication.run(Application.class, args);
    }
}