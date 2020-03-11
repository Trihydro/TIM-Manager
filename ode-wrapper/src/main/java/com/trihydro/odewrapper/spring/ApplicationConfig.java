package com.trihydro.odewrapper.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableWebMvc
@Configuration
@ComponentScan({ "com.trihydro.odewrapper.controller", "com.trihydro.library.helpers", "com.trihydro.library.service",
        "com.trihydro.library.tables" })
public class ApplicationConfig extends WebMvcConfigurerAdapter {
    public ApplicationConfig() {
        super();
    }
}