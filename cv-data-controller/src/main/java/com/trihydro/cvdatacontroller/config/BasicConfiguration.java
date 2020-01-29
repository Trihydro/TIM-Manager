package com.trihydro.cvdatacontroller.config;

import com.trihydro.library.model.ConfigProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("config")
@ComponentScan("com.trihydro.cvdatacontroller")
public class BasicConfiguration extends ConfigProperties {

}