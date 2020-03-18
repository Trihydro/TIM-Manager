package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.model.PopulatedRsu;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.config.EmailConfiguration;
import com.trihydro.tasks.models.RsuValidationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidateRsus implements Runnable {
    private DataTasksConfiguration config;
    private ActiveTimService activeTimService;
    private EmailConfiguration emailConfig;
    private EmailHelper mailHelper;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration _config, ActiveTimService _activeTimService,
            EmailConfiguration _emailConfig, EmailHelper _mailHelper) {
        config = _config;
        activeTimService = _activeTimService;
        emailConfig = _emailConfig;
        mailHelper = _mailHelper;
    }

    public void run() {
        // Fetch RSUs with ActiveTim records from Oracle
        List<PopulatedRsu> rsusWithRecords = activeTimService.getRsusWithActiveTims();

        if (rsusWithRecords.size() == 0) {
            return;
        }
        // TODO: Is there a best practice for retrieving and using a ThreadPool?
        // TODO: Change to thread pool
        ExecutorService workerThreadPool = Executors.newSingleThreadExecutor();

        List<ValidateRsu> tasks = new ArrayList<>();
        for (PopulatedRsu rsu : rsusWithRecords) {
            tasks.add(new ValidateRsu(rsu, config));
        }
        List<Future<RsuValidationResult>> futureResults = null;
        try {
            futureResults = workerThreadPool.invokeAll(tasks);
            awaitTerminationAfterShutdown(workerThreadPool);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> unresponsiveRsus = new ArrayList<>();
        List<RsuValidationResult> rsusWithErrors = new ArrayList<>();
        List<String> unexpectedErrors = new ArrayList<>();

        for (int i = 0; i < futureResults.size(); i++) {
            RsuValidationResult result = null;

            try {
                result = futureResults.get(i).get();
            } catch (Exception ex) {
                ex.printStackTrace();
                // "10.145.xx.xx: What went wrong..."
                unexpectedErrors.add(tasks.get(i).getRsu().getIpv4Address() + ": " + ex.getMessage());
                continue;
            }

            if (result.getRsuUnresponsive()) {
                unresponsiveRsus.add(result.getRsu());
                continue;
            }

            if (result.getCollisions().size() > 0 || result.getMissingFromRsu().size() > 0
                    || result.getUnaccountedForIndices().size() > 0) {
                rsusWithErrors.add(result);
            }
        }

        if (unresponsiveRsus.size() > 0 || rsusWithErrors.size() > 0 || unexpectedErrors.size() > 0) {
            String email = emailConfig.generateRsuSummaryEmail(unresponsiveRsus, rsusWithErrors, unexpectedErrors);

            try {
                mailHelper.SendEmail(config.getAlertAddresses(), null, "SDX Validation Results", email,
                        config.getMailPort(), config.getMailHost(), config.getFromEmail());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            // TODO add to config
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}