package com.trihydro.library.helpers;

import com.trihydro.library.model.ConfigProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableConfigurationProperties(ConfigProperties.class)
public class DbUtility {

    private static ConfigProperties dbConfig;

    public static void setConfig(ConfigProperties configProperties) {
        dbConfig = configProperties;
    }

    public static ConfigProperties getConfig() {
        return dbConfig;
    }
}