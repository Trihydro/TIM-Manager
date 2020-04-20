package com.trihydro.library.helpers;

import com.trihydro.library.model.SdwProps;

public class TestConfig implements SdwProps {

    private String sdwRestUrl;
    private String sdwApiKey;

    @Override
    public String getSdwApiKey() {
        return this.sdwApiKey;
    }

    public void setSdwApiKey(String sdwApiKey) {
        this.sdwApiKey = sdwApiKey;
    }

    public void setSdwRestUrl(String sdwRestUrl) {
        this.sdwRestUrl = sdwRestUrl;
    }

    @Override
    public String getSdwRestUrl() {
        return this.sdwRestUrl;
    }

}