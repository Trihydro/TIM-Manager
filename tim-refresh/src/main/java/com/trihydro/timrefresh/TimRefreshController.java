package com.trihydro.timrefresh;

import com.trihydro.library.model.ActiveTim;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.Logging_TimUpdateModel;
import com.trihydro.library.model.ResubmitTimException;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.timrefresh.config.TimRefreshConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TimRefreshController {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public Gson gson = new Gson();
    protected TimRefreshConfiguration configuration;
    private final Utility utility;
    private final EmailHelper emailHelper;
    private final ActiveTimService activeTimService;
    private final TimGenerationHelper timGenerationHelper;

    @Autowired
    public TimRefreshController(TimRefreshConfiguration configurationRhs, Utility _utility,
                                ActiveTimService _activeTimService, EmailHelper _emailHelper, TimGenerationHelper _timGenerationHelper) {
        configuration = configurationRhs;
        utility = _utility;
        activeTimService = _activeTimService;
        emailHelper = _emailHelper;
        timGenerationHelper = _timGenerationHelper;
    }

    /**
     * This method is intended to be run once a day, but can be configured to run at
     * any time. It will check for Active TIMs that are expiring within 24 hours and
     * issue new TIMs to the ODE for those that are expiring.
     */
    @Scheduled(cron = "${cron.expression}")
    public void performTaskUsingCron() {
        utility.logWithDate("Regular task performed using Cron at " + dateFormat.format(new Date()));

        // fetch Active_TIM that are expiring within 24 hrs
        List<TimUpdateModel> expiringTims = activeTimService.getExpiringActiveTims();

        utility.logWithDate(expiringTims.size() + " expiring TIMs found");
        List<Logging_TimUpdateModel> invalidTims = new ArrayList<Logging_TimUpdateModel>();
        List<ResubmitTimException> exceptionTims = new ArrayList<>();
        List<TimUpdateModel> timsToRefresh = new ArrayList<TimUpdateModel>();

        // loop through and issue new TIM to ODE
        for (TimUpdateModel aTim : expiringTims) {
            // find the bad and report them.
            // the rest can call out to TimGenerationHelper to finish

            // Validation
            if (!timGenerationHelper.isValidTim(aTim)) {
                invalidTims.add(new Logging_TimUpdateModel(aTim));
            } else {
                timsToRefresh.add(aTim);
            }
        }

        var resetSuccessful = true;

        // attempt to refresh TIMs and collect any exceptions
        if (!timsToRefresh.isEmpty()) {
            var activeTimIds = timsToRefresh.stream().map(ActiveTim::getActiveTimId).collect(Collectors.toList());
            // Reset expiration dates so they'll be updated after messages are processed.
            // Success isn't critical to proceed. We'll just end up with redundant resubmissions later on.
            resetSuccessful = activeTimService.resetActiveTimsExpirationDate(activeTimIds);
            exceptionTims = timGenerationHelper.resetTimStartTimeAndResubmitToOde(activeTimIds);
        }

        if (!invalidTims.isEmpty() || !exceptionTims.isEmpty() || !resetSuccessful) {
            String body = "";

            if (!resetSuccessful) {
                body += "An error occurred while resetting the expiration date(s) for the Active TIM(s)";
                body += "<br/><br/>";
            }

            if (!invalidTims.isEmpty()) {
                body += "The Tim Refresh application found invalid TIM(s) while attempting to refresh.";
                body += "<br/>";
                body += "The associated ActiveTim records are: <br/>";
                StringBuilder bodyBuilder = new StringBuilder(body);
                for (Logging_TimUpdateModel timUpdateModel : invalidTims) {
                    bodyBuilder.append(gson.toJson(timUpdateModel));
                    bodyBuilder.append("<br/><br/>");
                }
                body = bodyBuilder.toString();
            }

            if (!exceptionTims.isEmpty()) {
                body += "The TIM Refresh application ran into exceptions while attempting to resubmit TIMs. The following exceptions were found: ";
                body += "<br/>";
                StringBuilder bodyBuilder = new StringBuilder(body);
                for (ResubmitTimException rte : exceptionTims) {
                    bodyBuilder.append(gson.toJson(rte));
                    bodyBuilder.append("<br/>");
                }
                body = bodyBuilder.toString();
            }

            try {
                utility.logWithDate(
                    "Sending error email. The following TIM exceptions were found: " + gson.toJson(body));
                emailHelper.SendEmail(configuration.getAlertAddresses(), "TIM Refresh Exceptions", body);
            } catch (Exception e) {
                utility.logWithDate("Exception attempting to send email for invalid TIM:");
                e.printStackTrace();
            }
        }
    }
}