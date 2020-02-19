package com.trihydro.library.service;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.ConfigProperties;

public class CvDataServiceLibrary {

    protected static String CVRestUrl;

    public static void setConfig(ConfigProperties config) {
        DbUtility.setConfig(config);
    }

    public static void setCVRestUrl(String url) {
        CVRestUrl = url;
    }
}
