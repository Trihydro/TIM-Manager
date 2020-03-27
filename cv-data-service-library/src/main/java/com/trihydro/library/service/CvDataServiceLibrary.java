package com.trihydro.library.service;

import org.springframework.stereotype.Component;

@Component
public class CvDataServiceLibrary {

    protected static String CVRestUrl;

    // public static void setConfig(ConfigProperties config) {
    //     DbUtility.setConfig(config);
    // }

    public static void setCVRestUrl(String url) {
        CVRestUrl = url;
    }
}
