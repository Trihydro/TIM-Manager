package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.trihydro.cvdatacontroller.model.DataControllerConfigProperties;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.Utility;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
public class BaseController {

    private HikariDataSource hds = null;
    private HikariConfig config;
    private DataControllerConfigProperties dbConfig;

    private DateFormat utcFormatMilliSec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private DateFormat utcFormatSec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private DateFormat utcFormatMin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    protected DateFormat mstFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");

    @Autowired
    public void SetConfig(DataControllerConfigProperties props) {
        dbConfig = props;
    }

    public Connection GetConnectionPool() throws SQLException {

        // create pool if not already done
        if (hds == null) {

            TimeZone timeZone = TimeZone.getTimeZone("America/Denver");
            TimeZone.setDefault(timeZone);
            config = new HikariConfig();

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.setUsername(dbConfig.getDbUsername());
            config.setPassword(dbConfig.getDbPassword());
            config.setJdbcUrl(dbConfig.getDbUrl());
            config.setDriverClassName(dbConfig.getDbDriver());
            config.setMaximumPoolSize(20);// formula: connections = ((core_count*2) + effective_spindle_count)
                                          // https://stackoverflow.com/questions/28987540/why-does-hikaricp-recommend-fixed-size-pool-for-better-performance
                                          // we have 8 cores and are limiting the container to run on a single 'drive'
                                          // which gives us 17 pool size. we round up here
            config.setMaxLifetime(600000);// setting a maxLifetime of 10 minutes (defaults to 30), to help avoid
                                          // connection issues

            hds = new HikariDataSource(config);
        }

        // return a connection
        try {
            return hds.getConnection();
        } catch (SQLException ex) {
            String body = "The ODE Wrapper failed attempting to open a connection to ";
            body += dbConfig.getDbUrl();
            body += ". <br/>Exception message: ";
            body += ex.getMessage();
            body += "<br/>Stacktrace: ";
            body += ExceptionUtils.getStackTrace(ex);
            try {
                SendEmail(dbConfig.getAlertAddresses(), null, "ODE Wrapper Failed To Get Connection", body);
            } catch (Exception exception) {
                Utility.logWithDate("ODE Wrapper failed to open connection to " + dbConfig.getDbUrl()
                        + ", then failed to send email");
                exception.printStackTrace();
            }
            throw ex;
        }
    }

    public boolean updateOrDelete(PreparedStatement preparedStatement) {

        boolean result = false;

        try {
            if (preparedStatement.executeUpdate() > 0) {
                result = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Long executeAndLog(PreparedStatement preparedStatement, String type) {
        Long id = null;
        try {
            if (preparedStatement.executeUpdate() > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                try {
                    if (generatedKeys != null && generatedKeys.next()) {
                        id = generatedKeys.getLong(1);
                        Utility.logWithDate("------ Generated " + type + " " + id + " --------------");
                    }
                } finally {
                    try {
                        generatedKeys.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public Date convertDate(String incomingDate) {

        Date convertedDate = null;

        try {
            if (incomingDate != null) {
                if (incomingDate.contains("."))
                    convertedDate = utcFormatMilliSec.parse(incomingDate);
                else if (incomingDate.length() == 22)
                    convertedDate = utcFormatMin.parse(incomingDate);
                else
                    convertedDate = utcFormatSec.parse(incomingDate);
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        return convertedDate;
    }

    void SendEmail(String[] to, String[] bcc, String subject, String body) throws MailException, MessagingException {
        JavaMailSenderImpl mailSender = JavaMailSenderImplProvider.getJSenderImpl(dbConfig.getMailHost(),
                dbConfig.getMailPort());
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setSubject(subject);
        helper.setTo(to);
        helper.setFrom(dbConfig.getFromEmail());
        if (bcc != null)
            helper.setBcc(bcc);
        helper.setText(body, true);

        mailSender.send(mimeMessage);
    }
}
