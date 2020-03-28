package com.trihydro.library.service;

public class CvDataServiceLibrary {
    protected static String CVRestUrl;

    public static String getCVRestUrl() {
        return CVRestUrl;
    }

    public static void setCVRestUrl(String url) {
        CVRestUrl = url;
    }
}
