package com.trihydro.library.model;

public class TopicDataWrapper {
    private String topic;
    private String data;

    public String getData() {
        return data;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setData(String data) {
        this.data = data;
    }
}