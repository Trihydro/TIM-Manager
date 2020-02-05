package com.trihydro.library.model;

import us.dot.its.jpo.ode.model.OdeData;

public class TopicDataWrapper {
    private OdeData data;
    private String topic;
    private String originalString;

    public OdeData getData() {
        return data;
    }

    public String getOriginalString() {
        return originalString;
    }

    public void setOriginalString(String originalString) {
        this.originalString = originalString;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setData(OdeData data) {
        this.data = data;
    }
}