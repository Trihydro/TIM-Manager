package com.trihydro.library.helpers;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class JavaMailSenderImplProvider {
    public JavaMailSenderImpl getJSenderImpl(String host, int port) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        return mailSender;
    }
}