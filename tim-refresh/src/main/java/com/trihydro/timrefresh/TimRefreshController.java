package com.trihydro.timrefresh;

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
    private Utility utility;
    private EmailHelper emailHelper;
    private ActiveTimService activeTimService;
    private TimGenerationHelper timGenerationHelper;

    @Autowired
    public TimRefreshController(TimRefreshConfiguration configurationRhs, Utility _utility,
            ActiveTimService _activeTimService, EmailHelper _emailHelper, TimGenerationHelper _timGenerationHelper) {
        configuration = configurationRhs;
        utility = _utility;
        activeTimService = _activeTimService;
        emailHelper = _emailHelper;
        timGenerationHelper = _timGenerationHelper;
    }

    @Scheduled(cron = "${cron.expression}") // run at 1:00am every day
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

        // attempt to refresh TIMs and collect any exceptions
        if (timsToRefresh.size() > 0) {
            var activeTimIds = timsToRefresh.stream().map(x -> x.getActiveTimId()).collect(Collectors.toList());
            exceptionTims = timGenerationHelper.resubmitToOde(activeTimIds);
        }

        if (invalidTims.size() > 0 || exceptionTims.size() > 0) {
            String body = "";

            if (invalidTims.size() > 0) {
                body = "The Tim Refresh application found invalid TIM(s) while attempting to refresh.";
                body += "<br/>";
                body += "The associated ActiveTim records are: <br/>";
                for (Logging_TimUpdateModel timUpdateModel : invalidTims) {
                    body += gson.toJson(timUpdateModel);
                    body += "<br/><br/>";
                }
            }

            if (exceptionTims.size() > 0) {
                body += "The TIM Refresh application ran into exceptions while attempting to resubmit TIMs. The following exceptions were found: ";
                body += "<br/>";
                for (ResubmitTimException rte : exceptionTims) {
                    body += gson.toJson(rte);
                    body += "<br/>";
                }
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