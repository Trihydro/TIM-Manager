package com.trihydro.library.helpers;

import com.trihydro.library.model.ConfigProperties;

import org.springframework.stereotype.Component;

@Component
public class DbUtility {

    private static ConfigProperties dbConfig;

    public static void setConfig(ConfigProperties configProperties) {
        dbConfig = configProperties;
    }

    public static ConfigProperties getConfig() {
        return dbConfig;
    }
}