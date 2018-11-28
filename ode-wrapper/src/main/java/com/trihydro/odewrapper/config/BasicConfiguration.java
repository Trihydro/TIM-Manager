package com.trihydro.odewrapper.config;

import com.trihydro.library.model.TestConfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("config")
public class BasicConfiguration extends TestConfig {

}