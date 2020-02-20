package com.trihydro.odewrapper;

import com.google.gson.Gson;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.odewrapper.config.BasicConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    protected static BasicConfiguration configuration;
    public Gson gson = new Gson();

    @Autowired
    public void InjectDependencies(BasicConfiguration configurationRhs, Utility _utility) {
        configuration = configurationRhs;
        _utility.logWithDate("ODE Wrapper found the following configuration: " + gson.toJson(configuration));
        CvDataServiceLibrary.setConfig(configuration);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}