package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.OdeService;
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
    private OdeService odeService;
    private TimGenerationHelper timGenerationHelper;
    private ExecutorFactory executorFactory;
    private EmailFormatter emailFormatter;
    private EmailHelper mailHelper;
    private Utility utility;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration _config, ActiveTimService _activeTimService,
            RsuService _rsuService, RsuDataService _rsuDataService, OdeService _odeService,
            TimGenerationHelper _timGenerationHelper, ExecutorFactory _executorFactory, EmailFormatter _emailFormatter,
            EmailHelper _mailHelper, Utility _utility) {
        config = _config;
        activeTimService = _activeTimService;
        rsuService = _rsuService;
        rsuDataService = _rsuDataService;
        odeService = _odeService;
        timGenerationHelper = _timGenerationHelper;
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
            // Send email saying validation bombed
            utility.logWithDate("Error while validating RSUs:", this.getClass());
            ex.printStackTrace();
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }
    }

    private void validateRsus() throws Exception {
        List<String> unexpectedErrors = new ArrayList<>();
        List<RsuValidationRecord> validationRecords = new ArrayList<>();

        List<EnvActiveTim> activeTims = getActiveRsuTims();
        if (activeTims == null) {
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
        for (var rsuInfo : rsusToValidate) {
            validationRecords.add(new RsuValidationRecord(rsuInfo));
        }

        // Perform first validation pass
        validateRsus(validationRecords);

        // Pick out the RSUs with issues that we can try to correct automatically
        List<RsuValidationRecord> rsusWithResolvableIssues = new ArrayList<>();

        // Some TIMs may be live on multiple RSUs. By using a TreeSet, we ensure that
        // TIMs meant for multiple RSUs won't be re-submitted multiple times.
        TreeSet<EnvActiveTim> timsToResend = new TreeSet<>();

        boolean unresolvableIssueFound = false;
        for (var rsuRecord : validationRecords) {
            if (rsuRecord.getError() != null || rsuRecord.getValidationResults().size() != 1
                    || rsuRecord.getValidationResults().get(0) == null) {
                unresolvableIssueFound = true;
                continue;
            }

            var result = rsuRecord.getValidationResults().get(0);

            if (result.getRsuUnresponsive()) {
                unresolvableIssueFound = true;
            }

            if (result.getMissingFromRsu().size() == 0 && result.getStaleIndexes().size() == 0
                    && result.getUnaccountedForIndices().size() == 0) {
                continue;
            }

            rsusWithResolvableIssues.add(rsuRecord);

            // Resend Production TIMs that should be on this RSU but aren't
            for (var tim : result.getMissingFromRsu()) {
                if (tim.getEnvironment() == Environment.PROD) {
                    timsToResend.add(tim);
                }
            }

            // Resend Production TIMs that haven't been updated on this RSU
            for (var staleIndex : result.getStaleIndexes()) {
                if (staleIndex.getEnvTim().getEnvironment() == Environment.PROD) {
                    timsToResend.add(staleIndex.getEnvTim());
                }
            }

            // If this RSU has any indices that are unaccounted for, clear them
            if (result.getUnaccountedForIndices().size() > 0) {
                var rsu = new WydotRsu();
                rsu.setRsuTarget(rsuRecord.getRsuInformation().getIpv4Address());
                rsu.setRsuRetries(2);
                rsu.setRsuTimeout(3000);

                for (var index : result.getUnaccountedForIndices()) {
                    odeService.deleteTimFromRsu(rsu, index, config.getOdeUrl());
                }
            }
        }

        // Resubmit stale TIMs
        List<Long> activeTimIds = timsToResend.stream().map(x -> x.getActiveTim().getActiveTimId())
                .collect(Collectors.toList());
        var resubmitErrors = timGenerationHelper.resubmitToOde(activeTimIds);

        for (var error : resubmitErrors) {
            String message = String.format("Error resubmitting Active TIM %d. Error: %s", error.getActiveTimId(),
                    error.getExceptionMessage());
            utility.logWithDate(message, this.getClass());
            unexpectedErrors.add(message);
        }

        // Now that we've attempted to cleanup the RSU, perform second validation pass
        // Note that we only need to re-validate RSUs we attempted to correct
        if (rsusWithResolvableIssues.size() > 0) {
            validateRsus(rsusWithResolvableIssues);
        }

        // We should only send a validation summary if any inconsistencies were found
        // or if any errors occurred
        if (unexpectedErrors.size() > 0 || rsusWithResolvableIssues.size() > 0 || unresolvableIssueFound) {
            // Send validationRecords (all RSUs that were validated) to the email generator.
            String email = emailFormatter.generateRsuSummaryEmail(validationRecords, unexpectedErrors);

            try {
                mailHelper.SendEmail(config.getAlertAddresses(), null, "RSU Validation Results", email,
                        config.getMailPort(), config.getMailHost(), config.getFromEmail());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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
            try {
                var result = futureResults.get(i).get();
                rsusToValidate.get(i).addValidationResult(result);
            } catch (Exception e) {
                // Something went wrong, and the validation task for this RSU wasn't completed.
                String rsuIpv4Address = tasks.get(i).getRsu().getIpv4Address();
                String message = "Error while validating RSU " + rsuIpv4Address + ":\n" + e.toString();
                utility.logWithDate(message, this.getClass());
                rsusToValidate.get(i).setError(message);
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