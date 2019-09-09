package com.trihydro.timrefresh;

import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.timrefresh.config.BasicConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
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
}