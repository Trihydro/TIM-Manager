package com.trihydro.certexpiration;

import com.trihydro.certexpiration.config.CertExpirationConfiguration;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.service.RestTemplateProvider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@Import({ EmailHelper.class, RestTemplateProvider.class, CvDataServiceLibrary.class, JavaMailSenderImplProvider.class, Utility.class, ActiveTimService.class })
@SpringBootApplication
@EnableConfigurationProperties(CertExpirationConfiguration.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}