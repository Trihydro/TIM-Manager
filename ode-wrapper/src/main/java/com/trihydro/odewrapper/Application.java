package com.trihydro.odewrapper;

import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.odewrapper.config.BasicConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
}