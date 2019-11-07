package com.trihydro.timrefresh.config;

import com.trihydro.library.model.ConfigProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;

@Component
@ConfigurationProperties("config")
public class BasicConfiguration extends ConfigProperties {

    private TimeToLive sdwTtl;

    public TimeToLive getSdwTtl() {
        return sdwTtl;
    }

    public void setSdwTtl(String sdwTtl) {
        this.sdwTtl = TimeToLive.valueOf(sdwTtl);
    }
}