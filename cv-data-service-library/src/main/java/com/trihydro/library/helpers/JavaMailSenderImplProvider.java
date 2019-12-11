package com.trihydro.library.helpers;

import org.springframework.mail.javamail.JavaMailSenderImpl;

public class JavaMailSenderImplProvider {
    public static JavaMailSenderImpl getJSenderImpl(String host, int port) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        return mailSender;
    }
}