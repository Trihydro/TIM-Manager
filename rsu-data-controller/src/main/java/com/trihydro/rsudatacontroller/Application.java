package com.trihydro.rsudatacontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    // protected static BasicConfiguration configuration;

    // @Autowired
    // public void setConfiguration(BasicConfiguration configurationRhs) {
    //     configuration = configurationRhs;
    // }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}