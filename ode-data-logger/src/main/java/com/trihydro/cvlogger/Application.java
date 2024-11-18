package com.trihydro.cvlogger;

import com.trihydro.cvlogger.config.DataLoggerConfiguration;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.JsonToJavaConverter;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.service.RestTemplateProvider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@Import({ JsonToJavaConverter.class, RestTemplateProvider.class, CvDataServiceLibrary.class,
        JavaMailSenderImplProvider.class, Utility.class,
        EmailHelper.class, JavaMailSenderImplProvider.class })
@SpringBootApplication
@EnableConfigurationProperties(DataLoggerConfiguration.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}