package com.trihydro.library.service;

import com.trihydro.library.model.CVRestServiceProps;

import org.springframework.beans.factory.annotation.Autowired;

public class CvDataServiceLibrary {
    /**
     * @deprecated
     */
    protected static String CVRestUrl;

    /**
     * @deprecated
     * @return
     */
    public static String getCVRestUrl() {
        return CVRestUrl;
    }

    /**
     * @deprecated
     * @param url
     */
    public static void setCVRestUrl(String url) {
        CVRestUrl = url;
    }

    protected CVRestServiceProps config;

    @Autowired
    public void InjectDependencies(CVRestServiceProps _cvRestServviceProps) {
        this.config = _cvRestServviceProps;
    }
}
