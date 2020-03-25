package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RsuDataService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.config.EmailConfiguration;
import com.trihydro.tasks.models.EnvActiveTim;
import com.trihydro.tasks.models.Environment;
import com.trihydro.tasks.models.PopulatedRsu;
import com.trihydro.tasks.models.RsuValidationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidateRsus implements Runnable {
    private DataTasksConfiguration config;
    private ActiveTimService activeTimService;
    private RsuDataService rsuDataService;
    private EmailConfiguration emailConfig;
    private EmailHelper mailHelper;
    private Utility utility;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration _config, ActiveTimService _activeTimService,
            RsuDataService _rsuDataService, EmailConfiguration _emailConfig, EmailHelper _mailHelper,
            Utility _utility) {
        config = _config;
        activeTimService = _activeTimService;
        rsuDataService = _rsuDataService;
        emailConfig = _emailConfig;
        mailHelper = _mailHelper;
        utility = _utility;
    }

    public void run() {
        // The data structure being used here is temporary. Since we have RSUs shared
        // between our dev and prod environment, we need to fetch Active Tims from both
        // the dev and prod Oracle db. THEN we need to merge those records into the
        // same set while maintaining ordering before proceeding.
        TreeSet<EnvActiveTim> activeTims = new TreeSet<>();

        try {
            // Fetch records for dev
            for (ActiveTim activeTim : activeTimService.getActiveRsuTims(config.getCvRestServiceDev())) {
                activeTims.add(new EnvActiveTim(activeTim, Environment.DEV));
            }

            // Fetch records for prod
            for (ActiveTim activeTim : activeTimService.getActiveRsuTims(config.getCvRestServiceProd())) {
                activeTims.add(new EnvActiveTim(activeTim, Environment.PROD));
            }
        } catch (Exception ex) {
            utility.logWithDate("Unable to validate RSUs - error occurred while fetching Oracle records:");
            ex.printStackTrace();
            return;
        }

        // DEBUGGING
        // activeTims.forEach((record) -> {
        // System.out.println(record.getActiveTim().getRsuTarget() + " " +
        // record.getEnvironment() + " " + record.getActiveTim().getActiveTimId());
        // });

        // Organize ActiveTim records by RSU
        List<PopulatedRsu> rsusWithRecords = getRsusFromActiveTims(activeTims);

        if (rsusWithRecords.size() == 0) {
            return;
        }

        // DEBUGGING
        // rsusWithRecords.forEach((rec) -> {
        // System.out.println(rec.getIpv4Address() + ": " +
        // Integer.toString(rec.getRsuActiveTims().size()));
        // });

        // TODO: Is there a best practice for retrieving and using a ThreadPool?
        // TODO: Change to thread pool
        ExecutorService workerThreadPool = Executors.newSingleThreadExecutor();

        List<ValidateRsu> tasks = new ArrayList<>();
        for (PopulatedRsu rsu : rsusWithRecords) {
            tasks.add(new ValidateRsu(rsu, rsuDataService));
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
            if (!threadPool.awaitTermination(300, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private List<PopulatedRsu> getRsusFromActiveTims(TreeSet<EnvActiveTim> activeTims) {
        List<PopulatedRsu> rsusWithRecords = new ArrayList<>();
        PopulatedRsu rsu = null;

        for (EnvActiveTim record : activeTims) {
            if (rsu == null || !rsu.getIpv4Address().equals(record.getActiveTim().getRsuTarget())) {
                if (rsu != null) {
                    rsusWithRecords.add(rsu);
                }

                // Create new PopulatedRsu record
                rsu = new PopulatedRsu(record.getActiveTim().getRsuTarget());
            }
            rsu.getRsuActiveTims().add(record);
        }

        if (rsu != null) {
            rsusWithRecords.add(rsu);
        }

        return rsusWithRecords;
    }
}