package com.trihydro.cvdatacontroller.config;

import com.trihydro.library.model.ConfigProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("config")
@ComponentScan({"com.trihydro.cvdatacontroller", "com.trihydro.library.tables", "com.trihydro.library.helpers"})
public class BasicConfiguration extends ConfigProperties {

}