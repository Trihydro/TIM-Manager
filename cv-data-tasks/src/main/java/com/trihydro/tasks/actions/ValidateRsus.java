package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RsuDataService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.config.EmailConfiguration;
import com.trihydro.tasks.helpers.ExecutorFactory;
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
    private ExecutorFactory executorFactory;
    private EmailConfiguration emailConfig;
    private EmailHelper mailHelper;
    private Utility utility;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration _config, ActiveTimService _activeTimService,
            RsuDataService _rsuDataService, ExecutorFactory _executorFactory, EmailConfiguration _emailConfig,
            EmailHelper _mailHelper, Utility _utility) {
        config = _config;
        activeTimService = _activeTimService;
        rsuDataService = _rsuDataService;
        executorFactory = _executorFactory;
        emailConfig = _emailConfig;
        mailHelper = _mailHelper;
        utility = _utility;
    }

    public void run() {
        utility.logWithDate("ValidateRsus - Running...");

        try {
            validateRsus();
        } catch (Exception ex) {
            utility.logWithDate("Error while validating RSUs:");
            ex.printStackTrace();
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }
    }

    private void validateRsus() {
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

        // If there isn't anything to verify, exit early.
        if (activeTims.size() == 0) {
            return;
        }

        // DEBUGGING
        // activeTims.forEach((record) -> {
        // System.out.println(record.getActiveTim().getRsuTarget() + " " +
        // record.getEnvironment() + " " + record.getActiveTim().getActiveTimId());
        // });

        // Organize ActiveTim records by RSU
        List<PopulatedRsu> rsusWithRecords = getRsusFromActiveTims(activeTims);

        // DEBUGGING
        // rsusWithRecords.forEach((rec) -> {
        // System.out.println(rec.getIpv4Address() + ": " +
        // Integer.toString(rec.getRsuActiveTims().size()));
        // });

        ExecutorService workerThreadPool = executorFactory.getFixedThreadPool(config.getRsuValThreadPoolSize());

        // Map each RSU to an asynchronous "task" that will validate that RSU
        List<ValidateRsu> tasks = new ArrayList<>();
        for (PopulatedRsu rsu : rsusWithRecords) {
            tasks.add(new ValidateRsu(rsu, rsuDataService));
        }

        utility.logWithDate("Validating " + tasks.size() + " RSUs...");

        List<Future<RsuValidationResult>> futureResults = null;
        try {
            // Invoke all validation tasks, and wait for them to complete
            futureResults = workerThreadPool.invokeAll(tasks, config.getRsuValTimeoutSeconds(), TimeUnit.SECONDS);
            shutDownThreadPool(workerThreadPool);
        } catch (InterruptedException e) {
            utility.logWithDate("Error while executing validation tasks:");
            e.printStackTrace();
        }

        List<String> unresponsiveRsus = new ArrayList<>();
        List<RsuValidationResult> rsusWithErrors = new ArrayList<>();
        List<String> unexpectedErrors = new ArrayList<>();

        // Go through the validation results, and collect results to be reported
        for (int i = 0; i < futureResults.size(); i++) {
            RsuValidationResult result = null;

            try {
                result = futureResults.get(i).get();
            } catch (Exception ex) {
                String rsuIpv4Address = tasks.get(i).getRsu().getIpv4Address();
                // Something went wrong, and the validation task for this RSU wasn't completed.
                utility.logWithDate("Error while validating RSU " + rsuIpv4Address + ":");
                ex.printStackTrace();
                // "10.145.xx.xx: What went wrong..."
                unexpectedErrors.add(rsuIpv4Address + ": " + ex.toString() + " - " + ex.getMessage());
                continue;
            }

            // Check if we were able to initiate a SNMP session with the RSU
            if (result.getRsuUnresponsive()) {
                unresponsiveRsus.add(result.getRsu());
                continue;
            }

            // We were able to validate this RSU. If any oddities were found, queue this RSU
            // for the report
            if (result.getCollisions().size() > 0 || result.getMissingFromRsu().size() > 0
                    || result.getUnaccountedForIndices().size() > 0 || result.getStaleIndexes().size() > 0) {
                rsusWithErrors.add(result);
            }
        }

        // If we have any metrics to report...
        if (unresponsiveRsus.size() > 0 || rsusWithErrors.size() > 0 || unexpectedErrors.size() > 0) {
            // ... generate and send email
            String email = emailConfig.generateRsuSummaryEmail(unresponsiveRsus, rsusWithErrors, unexpectedErrors);

            try {
                mailHelper.SendEmail(config.getAlertAddresses(), null, "SDX Validation Results", email,
                        config.getMailPort(), config.getMailHost(), config.getFromEmail());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void shutDownThreadPool(ExecutorService threadPool) {
        threadPool.shutdown(); // Tell threadpool to stop accepting tasks and shut down
        try {
            if (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {
                // Threadpool took too long to shut down. Force close (may yield incomplete
                // tasks)
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // This method groups activeTim records by RSU (specifically, the RSU's ipv4
    // address)
    private List<PopulatedRsu> getRsusFromActiveTims(TreeSet<EnvActiveTim> activeTims) {
        List<PopulatedRsu> rsusWithRecords = new ArrayList<>();
        PopulatedRsu rsu = null;

        // Due to the TreeSet, the records in activeTims are sorted by rsuTarget.
        for (EnvActiveTim record : activeTims) {
            // If we don't have an RSU yet, or this record is the first one for the next
            // RSU...
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