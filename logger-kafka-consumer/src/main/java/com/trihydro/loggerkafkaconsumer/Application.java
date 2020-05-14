package com.trihydro.loggerkafkaconsumer;

import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.JsonToJavaConverter;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.tables.BsmOracleTables;
import com.trihydro.library.tables.DriverAlertOracleTables;
import com.trihydro.library.tables.TimOracleTables;
import com.trihydro.loggerkafkaconsumer.config.LoggerConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@Import({ Utility.class, DbInteractions.class, JsonToJavaConverter.class, TimOracleTables.class, BsmOracleTables.class,
		SQLNullHandler.class, DriverAlertOracleTables.class, EmailHelper.class, JavaMailSenderImplProvider.class })
@SpringBootApplication
@EnableConfigurationProperties(LoggerConfiguration.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
