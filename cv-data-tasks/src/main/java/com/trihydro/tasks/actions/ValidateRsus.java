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
import com.trihydro.library.service.RsuService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.helpers.EmailFormatter;
import com.trihydro.tasks.helpers.ExecutorFactory;
import com.trihydro.tasks.models.EnvActiveTim;
import com.trihydro.tasks.models.Environment;
import com.trihydro.tasks.models.RsuInformation;
import com.trihydro.tasks.models.RsuValidationRecord;
import com.trihydro.tasks.models.RsuValidationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidateRsus implements Runnable {
    private DataTasksConfiguration config;
    private ActiveTimService activeTimService;
    private RsuService rsuService;
    private RsuDataService rsuDataService;
    private ExecutorFactory executorFactory;
    private EmailFormatter emailFormatter;
    private EmailHelper mailHelper;
    private Utility utility;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration _config, ActiveTimService _activeTimService,
            RsuService _rsuService, RsuDataService _rsuDataService, ExecutorFactory _executorFactory,
            EmailFormatter _emailFormatter, EmailHelper _mailHelper, Utility _utility) {
        config = _config;
        activeTimService = _activeTimService;
        rsuService = _rsuService;
        rsuDataService = _rsuDataService;
        executorFactory = _executorFactory;
        emailFormatter = _emailFormatter;
        mailHelper = _mailHelper;
        utility = _utility;
    }

    public void run() {
        utility.logWithDate("Running...", this.getClass());

        try {
            validateRsus();
        } catch (Exception ex) {
            utility.logWithDate("Error while validating RSUs:", this.getClass());
            ex.printStackTrace();
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }
    }

    private void validateRsus() {
        List<String> unexpectedErrors = new ArrayList<>();
        List<RsuValidationRecord> validationRecords = new ArrayList<>();

        try {
            List<EnvActiveTim> activeTims = getActiveRsuTims();
        if(activeTims == null) {
            // Cannot proceed with validation.
            return;
        }

        // Organize ActiveTim records by RSU
        List<RsuInformation> rsusToValidate = getRsusFromActiveTims(activeTims);

        // rsusToValidate now contains a list of all RSUs that have at least one active
        // TIM record.
        // We still need to validate RSUs that may not have active TIMs right now.
        // Fetch all RSUs, and add any that are missing to rsusToValidate
        try {
            var allRsus = rsuService.selectAll();
            for (var rsu : allRsus) {
                var popRsu = new RsuInformation(rsu);
                if (!rsusToValidate.contains(popRsu)) {
                    rsusToValidate.add(popRsu);
                }
            }
        } catch (Exception ex) {
            utility.logWithDate("Unable to fetch all RSUs - will proceed with partial validation", this.getClass());

            unexpectedErrors.add("Error occurred while fetching all RSUs - "
                    + "unable to validate any RSUs that don't have an existing, active TIM. Error:\n" + ex.toString());
        }

        // If there isn't anything to verify, exit early.
        if (rsusToValidate.size() == 0) {
            return;
        }

        // Create a validation record (or log) for each RSU to be verified
        for(var rsuInfo : rsusToValidate) {
            validationRecords.add(new RsuValidationRecord(rsuInfo));
        }

        // Perform first validation pass
        validateRsus(validationRecords);

        // Pick out the RSUs with issues that we can try to correct automatically
        List<RsuValidationRecord> rsusWithResolvableIssues = new ArrayList<>();
        for(var rsuRecord : validationRecords)
        {
            if(rsuRecord.getError() != null || 
                rsuRecord.getValidationResults().size() != 1 ||
                rsuRecord.getValidationResults().get(0) == null) {
                continue;
            }
        }

        // ExecutorService workerThreadPool = executorFactory.getFixedThreadPool(config.getRsuValThreadPoolSize());

        // // Map each RSU to an asynchronous "task" that will validate that RSU
        // List<ValidateRsu> tasks = new ArrayList<>();
        // for (RsuInformation rsu : rsusToValidate) {
        //     tasks.add(new ValidateRsu(rsu, rsuDataService));
        // }

        // utility.logWithDate("Validating " + tasks.size() + " RSUs...", this.getClass());

        // List<Future<RsuValidationResult>> futureResults = null;
        // try {
        //     // Invoke all validation tasks, and wait for them to complete
        //     futureResults = workerThreadPool.invokeAll(tasks, config.getRsuValTimeoutSeconds(), TimeUnit.SECONDS);
        //     shutDownThreadPool(workerThreadPool);
        // } catch (InterruptedException e) {
        //     utility.logWithDate("Error while executing validation tasks:", this.getClass());
        //     e.printStackTrace();
        // }

        // // Go through the validation results, and collect results to be reported
        // for (int i = 0; i < futureResults.size(); i++) {
        //     RsuValidationResult result = null;

        //     try {
        //         result = futureResults.get(i).get();
        //     } catch (Exception e) {
        //         String rsuIpv4Address = tasks.get(i).getRsu().getIpv4Address();
        //         // Something went wrong, and the validation task for this RSU wasn't completed.
        //         utility.logWithDate("Error while validating RSU " + rsuIpv4Address + ":", this.getClass());
        //         e.printStackTrace();
        //         // "10.145.xx.xx: What went wrong..."
        //         unexpectedErrors.add(rsuIpv4Address + ": " + e.toString() + " - " + e.getMessage());
        //         continue;
        //     }

        //     // Check if we were able to initiate a SNMP session with the RSU
        //     if (result.getRsuUnresponsive()) {
        //         unresponsiveRsus.add(result.getRsu());
        //         continue;
        //     }

        //     // We were able to validate this RSU. If any oddities were found, queue this RSU
        //     // for the report
        //     if (result.getCollisions().size() > 0 || result.getMissingFromRsu().size() > 0
        //             || result.getUnaccountedForIndices().size() > 0 || result.getStaleIndexes().size() > 0) {
        //         rsusWithErrors.add(result);
        //     }
        // }

        // // If we have any metrics to report...
        // if (unresponsiveRsus.size() > 0 || rsusWithErrors.size() > 0 || unexpectedErrors.size() > 0) {
        //     // ... generate and send email
        //     String email = emailFormatter.generateRsuSummaryEmail(unresponsiveRsus, rsusWithErrors, unexpectedErrors);

        //     try {
        //         mailHelper.SendEmail(config.getAlertAddresses(), null, "RSU Validation Results", email,
        //                 config.getMailPort(), config.getMailHost(), config.getFromEmail());
        //     } catch (Exception ex) {
        //         ex.printStackTrace();
        //     }
        }
        }catch(

    Exception ex)
    {
        // An error occurred while we were performing validation.
        unexpectedErrors.add("An error occurred during the validation process. Validation terminated early. Error:\n"
                + ex.toString());
    }
    // TODO: send validation summary email (may be partial if validation failed
    // early)
    }

    /**
     * Performs a validation task for each RSU asynchronously. Adds the validation
     * result to the RSU's record if validation completed, or null if not.
     * 
     * @param rsusToValidate RSUs that should be validated. All RsuValidationRecords
     *                       will have an additional RsuValidationResult after this
     *                       task completes.
     * @throws Exception
     */
    private void validateRsus(List<RsuValidationRecord> rsusToValidate) throws Exception {
        ExecutorService workerThreadPool = executorFactory.getFixedThreadPool(config.getRsuValThreadPoolSize());

        // Map each RSU to an asynchronous task that will perform the validation for
        // that RSU
        List<ValidateRsu> tasks = new ArrayList<>();
        for (RsuValidationRecord rsu : rsusToValidate) {
            tasks.add(new ValidateRsu(rsu.getRsuInformation(), rsuDataService));
        }

        utility.logWithDate("Validating " + tasks.size() + " RSUs...", this.getClass());

        List<Future<RsuValidationResult>> futureResults = null;
        try {
            // Invoke all validation tasks, and wait for them to complete
            futureResults = workerThreadPool.invokeAll(tasks, config.getRsuValTimeoutSeconds(), TimeUnit.SECONDS);
            shutDownThreadPool(workerThreadPool);
        } catch (InterruptedException e) {
            utility.logWithDate("Error while executing validation tasks:", this.getClass());
            e.printStackTrace();
        }

        if (futureResults == null || futureResults.size() != rsusToValidate.size()) {
            throw new Exception("Not all validation tasks were scheduled. Validation cannot be completed.");
        }

        // Go through each result, and update the corresponding validation record
        for (int i = 0; i < futureResults.size(); i++) {
            RsuValidationResult result = null;

            try {
                result = futureResults.get(i).get();
            } catch (Exception e) {
                // Something went wrong, and the validation task for this RSU wasn't completed.
                String rsuIpv4Address = tasks.get(i).getRsu().getIpv4Address();
                String message = "Error while validating RSU " + rsuIpv4Address + ":\n" + e.toString();
                utility.logWithDate(message, this.getClass());
                rsusToValidate.get(i).setError(message);
            }

            // If the validation task completed, we have a non-null object.
            // If the validation task bombed or was cancelled, the result is null
            rsusToValidate.get(i).addValidationResult(result);
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

    private List<EnvActiveTim> getActiveRsuTims() {
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
        } catch (Exception ex) {
            utility.logWithDate("Unable to validate RSUs - error occurred while fetching Oracle records from DEV:",
                    this.getClass());
            ex.printStackTrace();
            return null;
        }

        try {
            // Fetch records for prod
            for (ActiveTim activeTim : activeTimService.getActiveRsuTims(config.getCvRestServiceProd())) {
                activeTims.add(new EnvActiveTim(activeTim, Environment.PROD));
            }
        } catch (Exception ex) {
            utility.logWithDate("Unable to validate RSUs - error occurred while fetching Oracle records from PROD:",
                    this.getClass());
            ex.printStackTrace();
            return null;
        }

        return new ArrayList<EnvActiveTim>(activeTims);
    }

    // This method groups activeTim records by RSU (specifically, the RSU's ipv4
    // address)
    private List<RsuInformation> getRsusFromActiveTims(List<EnvActiveTim> activeTims) {
        List<RsuInformation> rsusWithRecords = new ArrayList<>();
        RsuInformation rsu = null;

        // Due to the TreeSet, the records in activeTims are sorted by rsuTarget.
        for (EnvActiveTim record : activeTims) {
            // If we don't have an RSU yet, or this record is the first one for the next
            // RSU...
            if (rsu == null || !rsu.getIpv4Address().equals(record.getActiveTim().getRsuTarget())) {
                if (rsu != null) {
                    rsusWithRecords.add(rsu);
                }

                // Create new PopulatedRsu record
                rsu = new RsuInformation(record.getActiveTim().getRsuTarget());
            }
            rsu.getRsuActiveTims().add(record);
        }

        if (rsu != null) {
            rsusWithRecords.add(rsu);
        }

        return rsusWithRecords;
    }
}