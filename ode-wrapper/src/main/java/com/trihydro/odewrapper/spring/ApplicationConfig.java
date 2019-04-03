package com.trihydro.odewrapper.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = { "com.trihydro.odewrapper.controller" })
public class ApplicationConfig extends WebMvcConfigurerAdapter {

    public ApplicationConfig() {
        super();
    }

}