package com.trihydro.cvlogger.app.services;

import com.trihydro.library.model.ConfigProperties;

import org.springframework.mail.javamail.JavaMailSenderImpl;

public class JavaMailSenderImplProvider {

    public static JavaMailSenderImpl getJSenderImpl(ConfigProperties config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getMailHost());
        mailSender.setPort(config.getMailPort());

        return mailSender;
    }

}