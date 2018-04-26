package com.trihydro.odewrapper.spring;

import org.springframework.context.annotation.ComponentScan;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
@ComponentScan("com.trihydro.odewrapper.spring")
public class WebConfig extends WebMvcConfigurerAdapter {

    public WebConfig() {
        super();
    }

}