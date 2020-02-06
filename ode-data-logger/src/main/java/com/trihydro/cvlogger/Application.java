package com.trihydro.cvlogger;

import com.trihydro.cvlogger.config.DataLoggerConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.trihydro.cvlogger","com.trihydro.library.helpers"})
@SpringBootApplication
@EnableConfigurationProperties(DataLoggerConfiguration.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}