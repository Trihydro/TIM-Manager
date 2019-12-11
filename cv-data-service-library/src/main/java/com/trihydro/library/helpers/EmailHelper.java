package com.trihydro.library.helpers;

import com.trihydro.library.model.ConfigProperties;

import org.springframework.mail.SimpleMailMessage;

public class EmailHelper {
    public static void SendEmail(String[] to, String[] bcc, String subject, String body, ConfigProperties properties) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setTo(to);
        message.setFrom(properties.getFromEmail());
        if (bcc != null)
            message.setBcc(bcc);
        message.setText(body);

        JavaMailSenderImplProvider.getJSenderImpl(properties.getMailHost(), properties.getMailPort()).send(message);
    }
}