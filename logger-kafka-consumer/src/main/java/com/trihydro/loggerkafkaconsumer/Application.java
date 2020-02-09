package com.trihydro.loggerkafkaconsumer;

import com.trihydro.loggerkafkaconsumer.config.LoggerConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.trihydro.loggerkafkaconsumer","com.trihydro.library.helpers", "com.trihydro.library.tables"})
@SpringBootApplication
@EnableConfigurationProperties(LoggerConfiguration.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
