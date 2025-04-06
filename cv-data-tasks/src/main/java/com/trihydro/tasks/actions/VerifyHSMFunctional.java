package com.trihydro.tasks.actions;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.models.SignTimModel;
import com.trihydro.tasks.models.hsmresponse.HsmResponse;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VerifyHSMFunctional implements Runnable {
    private DataTasksConfiguration config;
    private Utility utility;
    private RestTemplateProvider restTemplateProvider;
    private EmailHelper mailHelper;

    public Date errorLastSent = null;
    private SignTimModel signTimModel;
    private HttpEntity<SignTimModel> entity;

    private static final int sigValidityOverride = 36000000;
    private static final String message = "AB+AhHAVP6+uw89IRtyVmrEPd12bAwnCgIt80Vzq0of/+T8inWB9AKEH+c036tNAxbX6cGotYRIjiMxbOb9KvfZM2jRlAAAAABQEW+aK51aUP95n4AEIUWd7wquPelB+HceDETPJJJ/o+hfPUkhdHQFjWSIKz4TapJENJz/9BBCQAgCe7rs2AA==";

    @Autowired
    public void InjectDependencies(DataTasksConfiguration _configuration, Utility _utility,
            RestTemplateProvider _restTemplateProvider, EmailHelper _emailHelper) {
        config = _configuration;
        utility = _utility;
        restTemplateProvider = _restTemplateProvider;
        mailHelper = _emailHelper;

        signTimModel = new SignTimModel();
        signTimModel.setMessage(message);
        signTimModel.setSigValidity(sigValidityOverride);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        entity = new HttpEntity<SignTimModel>(signTimModel, headers);
    }

    public void run() {
        log.info("Running...");
        try {
            // ping HSM
            var response = restTemplateProvider.GetRestTemplate_NoErrors().exchange(config.getHsmUrl() + "/signtim/",
                    HttpMethod.POST, entity, HsmResponse.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.info("HSM is not responsive! If an email should be sent, it will be shortly.");
            }

            if (response.getStatusCode() == HttpStatus.OK) {
                if (errorLastSent != null) {
                    // send an email telling us its back up
                    String email = "HSM Functional Tester was successful in attempting to sign a TIM";
                    mailHelper.SendEmail(config.getAlertAddresses(), "HSM Back Up", email);
                    log.info("HSM is back up! Email sent.");
                } else {
                    log.info("HSM is up!");
                }
                errorLastSent = null;
            } else if (shouldSendEmail(errorLastSent)) {
                // got an error, email the team
                errorLastSent = new Date();
                String email = "HSM Functional Tester encountered an error while attempting to sign a TIM. The response from the HSM at ";
                email += config.getHsmUrl() + "/signtim/";
                email += " was Http Status " + response.getStatusCodeValue();
                email += ". The body of the response is as follows: ";
                email += "<br/><br/>";
                email += response.getBody();

                mailHelper.SendEmail(config.getAlertAddresses(), "HSM Error", email);
            }
        } catch (Exception e) {
            log.error("Exception", e);
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.

            try {
                errorLastSent = new Date();
                String email = "HSM Functional Tester encountered an exception while attempting to sign a TIM:";
                email += "<br/><br/>";
                email += e.getMessage();
                mailHelper.SendEmail(config.getAlertAddresses(), "HSM Error", email);
            } catch (Exception subEx) {
                log.error("Exception", e);
            }
        }
    }

    private boolean shouldSendEmail(Date lastSent) {
        if (lastSent == null) {
            return true;
        }

        var currentDate = new Date();
        var diffInMillis = Math.abs(currentDate.getTime() - lastSent.getTime());
        var diffInMins = TimeUnit.MINUTES.convert(diffInMillis, TimeUnit.MILLISECONDS);
        if (diffInMins > config.getHsmErrorEmailFrequencyMinutes()) {
            return true;
        }

        log.info("Email should not be sent at this time");
        return false;
    }
}
