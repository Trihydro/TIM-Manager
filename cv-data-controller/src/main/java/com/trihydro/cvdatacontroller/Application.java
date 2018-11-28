package com.trihydro.cvdatacontroller;

import com.trihydro.cvdatacontroller.config.BasicConfiguration;
import com.trihydro.library.service.CvDataServiceLibrary;

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