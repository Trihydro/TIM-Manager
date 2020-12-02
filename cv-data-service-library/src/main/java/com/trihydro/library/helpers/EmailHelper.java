package com.trihydro.library.helpers;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailHelper {

    private JavaMailSenderImplProvider mailProvider;

    @Autowired
    public void InjectDependencies(JavaMailSenderImplProvider _mailProvider) {
        mailProvider = _mailProvider;
    }

    /**
     * Sends an email with the given parameters
     * 
     * @param to         Array of emails to send email to
     * @param bcc        BCC email list
     * @param subject    Subject of the email
     * @param body       Body of the email. Can include html
     * @param mailPort   The port of the mail server to use when sending emails
     * @param mailHost   The host ip of the mail server
     * @param from       The email address to send this message from
     * @throws MailException
     * @throws MessagingException
     */
    public void SendEmail(String[] to, String[] bcc, String subject, String body, Integer mailPort, String mailHost,
            String from) throws MailException, MessagingException {
        JavaMailSenderImpl mailSender = mailProvider.getJSenderImpl(mailHost, mailPort);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setSubject(subject);
        helper.setTo(to);
        helper.setFrom(from);
        if (bcc != null)
            helper.setBcc(bcc);
        helper.setText(body, true);

        mailSender.send(mimeMessage);
    }

    /**
     * Sends an email stating the provided container was forced to restart.
     * @param to            Array of emails to send email to
     * @param mailPort      The port of the mail server to use when sending emails
     * @param mailHost      The host ip of the mail server
     * @param from          The email address to send this message from
     * @param containerInfo A descriptor of the container calling this function
     * @throws MailException
     * @throws MessagingException
     */
    public void ContainerRestarted(String[] to, Integer mailPort, String mailHost, String from, String containerInfo)
            throws MailException, MessagingException {
        String body = "The following container ran into an unrecoverable exception, causing a container restart:<br/>";
        body += containerInfo;
        SendEmail(to, null, "Container Error Caused Restart", body, mailPort, mailHost, from);
    }
}