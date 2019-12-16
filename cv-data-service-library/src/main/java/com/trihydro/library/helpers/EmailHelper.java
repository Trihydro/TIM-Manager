package com.trihydro.library.helpers;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.trihydro.library.model.ConfigProperties;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

public class EmailHelper {
    /**
     * Sends an email with the given parameters
     * @param to Array of emails to send email to
     * @param bcc BCC email list
     * @param subject Subject of the email
     * @param body Body of the email. Can include html
     * @param properties the ConfigProperties object containing the fromEmail, mailHost, and mailPort
     * @throws MailException
     * @throws MessagingException
     */
    public static void SendEmail(String[] to, String[] bcc, String subject, String body, ConfigProperties properties)
            throws MailException, MessagingException  {
        JavaMailSenderImpl mailSender = JavaMailSenderImplProvider.getJSenderImpl(properties.getMailHost(),
                properties.getMailPort());
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setSubject(subject);
        helper.setTo(to);
        helper.setFrom(properties.getFromEmail());
        if (bcc != null)
            helper.setBcc(bcc);
        helper.setText(body, true);

        mailSender.send(mimeMessage);
    }
}