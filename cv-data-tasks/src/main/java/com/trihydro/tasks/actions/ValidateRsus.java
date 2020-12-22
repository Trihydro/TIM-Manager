package com.trihydro.tasks.actions;

import java.util.ArrayList;
import java.util.Comparator;
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
import com.trihydro.tasks.models.RsuInformation;
import com.trihydro.tasks.models.RsuValidationRecord;
import com.trihydro.tasks.models.RsuValidationResult;

import org.apache.commons.lang3.StringUtils;
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

    private boolean delaySecondValidation = true;

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

    public void setDelaySecondValidation(boolean shouldDelay) {
        this.delaySecondValidation = shouldDelay;
    }

    public void run() {
        utility.logWithDate("Running...", this.getClass());

        try {
            validateRsus();
        } catch (Exception ex) {
            var msg = "An unexpected error occurred that prevented RSU validation from completing.\n";
            msg += ex.getMessage();
            utility.logWithDate(msg, this.getClass());

            try {
                mailHelper.SendEmail(config.getAlertAddresses(), "RSU Validation Error", msg);
            } catch (Exception mailException) {
                mailException.printStackTrace();
            }
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }
    }

    private void validateRsus() throws Exception {
        List<String> unexpectedErrors = new ArrayList<>();
        List<RsuValidationRecord> validationRecords = new ArrayList<>();

        List<ActiveTim> activeTims = getActiveRsuTims();

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
            var msg = "Unable to find any RSUs to validate.";
            utility.logWithDate(msg, this.getClass());

            try {
                mailHelper.SendEmail(config.getAlertAddresses(), "RSU Validation Error", msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

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
        TreeSet<ActiveTim> timsToResend = new TreeSet<>(new Rsu_ActiveTim_Comparator());

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
                continue;
            }

            if (result.getMissingFromRsu().size() == 0 && result.getStaleIndexes().size() == 0
                    && result.getUnaccountedForIndices().size() == 0) {
                continue;
            }

            rsusWithResolvableIssues.add(rsuRecord);

            // Resend Production TIMs that should be on this RSU but aren't
            for (var tim : result.getMissingFromRsu()) {
                timsToResend.add(tim);
            }

            // Resend Production TIMs that haven't been updated on this RSU
            for (var staleIndex : result.getStaleIndexes()) {
                timsToResend.add(staleIndex.getActiveTim());
            }

            // If this RSU has any indices that are unaccounted for, clear them
            if (result.getUnaccountedForIndices().size() > 0) {
                var rsu = new WydotRsu();
                rsu.setRsuTarget(rsuRecord.getRsuInformation().getIpv4Address());
                rsu.setRsuRetries(2);
                rsu.setRsuTimeout(3000);

                for (var index : result.getUnaccountedForIndices()) {
                    var exMsg = odeService.deleteTimFromRsu(rsu, index, config.getOdeUrl());
                    if (StringUtils.isNotBlank(exMsg)) {
                        unexpectedErrors.add(exMsg);
                    }
                }
            }
        }

        // Resubmit stale TIMs
        List<Long> activeTimIds = timsToResend.stream().map(x -> x.getActiveTimId()).collect(Collectors.toList());

        if (activeTimIds.size() > 0) {
            var resubmitErrors = timGenerationHelper.resubmitToOde(activeTimIds);

            for (var error : resubmitErrors) {
                String message = String.format("Error resubmitting Active TIM %d. Error: %s", error.getActiveTimId(),
                        error.getExceptionMessage());
                utility.logWithDate(message, this.getClass());
                unexpectedErrors.add(message);
            }
        }

        // Now that we've attempted to cleanup the RSU, perform second validation pass
        // Note that we only need to re-validate RSUs we attempted to correct
        if (rsusWithResolvableIssues.size() > 0) {
            // Wait a period of time to allow any corrective actions to propagate before
            // we perform the second validation attempt.
            if (delaySecondValidation) {
                Thread.sleep(config.getRsuValidationDelaySeconds() * 1000);
            }

            validateRsus(rsusWithResolvableIssues);
        }

        // We should only send a validation summary if any inconsistencies were found
        // or if any errors occurred
        if (unexpectedErrors.size() > 0 || rsusWithResolvableIssues.size() > 0 || unresolvableIssueFound) {
            // Send validationRecords (all RSUs that were validated) to the email generator.
            String email = emailFormatter.generateRsuSummaryEmail(validationRecords, unexpectedErrors);

            try {
                mailHelper.SendEmail(config.getAlertAddresses(), "RSU Validation Results", email);
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
                String rsuIpv4Address = tasks.get(i).getIpv4Address();
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

    private List<ActiveTim> getActiveRsuTims() {
        List<ActiveTim> activeTims = new ArrayList<>();

        try {
            // Fetch records for prod
            activeTims = activeTimService.getActiveRsuTims(config.getCvRestService());
        } catch (Exception ex) {
            utility.logWithDate("Unable to validate RSUs - error occurred while fetching Oracle records from PROD:",
                    this.getClass());
            ex.printStackTrace();
            return null;
        }

        return activeTims;
    }

    // This method groups activeTim records by RSU (specifically, the RSU's ipv4
    // address)
    private List<RsuInformation> getRsusFromActiveTims(List<ActiveTim> activeTims) {
        List<RsuInformation> rsusWithRecords = new ArrayList<>();
        RsuInformation rsu = null;

        // Due to the ORDER BY in our fetch from Oracle, the records in activeTims are
        // sorted by rsuTarget.
        for (ActiveTim record : activeTims) {
            // If we don't have an RSU yet, or this record is the first one for the next
            // RSU...
            if (rsu == null || !rsu.getIpv4Address().equals(record.getRsuTarget())) {
                if (rsu != null) {
                    rsusWithRecords.add(rsu);
                }

                // Create new PopulatedRsu record
                rsu = new RsuInformation(record.getRsuTarget());
            }
            rsu.getRsuActiveTims().add(record);
        }

        if (rsu != null) {
            rsusWithRecords.add(rsu);
        }

        return rsusWithRecords;
    }

    class Rsu_ActiveTim_Comparator implements Comparator<ActiveTim> {
        public int compare(ActiveTim o1, ActiveTim o2) {
            if (o1 == null || o2 == null) {
                if (o1 == null && o2 == null)
                    return 0;
                else if (o1 == null)
                    return -1;
                else
                    return 1;
            }

            int result = 0;

            // Compare activeTimId
            if (o1.getActiveTimId() == null)
                result = o2.getActiveTimId() == null ? 0 : -1;
            else
                result = o1.getActiveTimId().compareTo(o2.getActiveTimId());

            if (result != 0)
                return result;

            // Compare rsuTarget
            if (o1.getRsuTarget() == null)
                result = o2.getRsuTarget() == null ? 0 : -1;
            else
                result = o1.getRsuTarget().compareTo(o2.getRsuTarget());

            if (result != 0)
                return result;

            // Compare index
            if (o1.getRsuIndex() == null)
                result = o2.getRsuIndex() == null ? 0 : -1;
            else
                result = o1.getRsuIndex().compareTo(o2.getRsuIndex());

            return result;
        }
    }
}