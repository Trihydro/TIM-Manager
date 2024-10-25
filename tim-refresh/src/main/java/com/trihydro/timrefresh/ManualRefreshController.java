package com.trihydro.timrefresh;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.timrefresh.config.TimRefreshConfiguration;

import springfox.documentation.annotations.ApiIgnore;

import com.trihydro.timrefresh.Models.ManualRefresh;

@CrossOrigin
@RestController
@RequestMapping("refresh")
@ApiIgnore
public class ManualRefreshController {

    protected TimRefreshConfiguration configuration;
    private ActiveTimService activeTimService;
    private TimGenerationHelper timGenerationHelper;

    @Autowired
    public ManualRefreshController(TimRefreshConfiguration configurationRhs, Utility _utility,
            ActiveTimService _activeTimService, EmailHelper _emailHelper, TimGenerationHelper _timGenerationHelper) {
        configuration = configurationRhs;
        activeTimService = _activeTimService;
        timGenerationHelper = _timGenerationHelper;
    }

    @RequestMapping(value = "/manual", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<Boolean> GetExpiringActiveTims(@RequestBody ManualRefresh manualRefresh) {
        // Reset expiration dates so they'll be updated after messages are processed.
        // Success isn't critical to proceed. We'll just end up with redundant
        // resubmissions later on.
        var resetSuccessful = activeTimService.resetActiveTimsExpirationDate(manualRefresh.getActiveTimIds());
        var exceptionTims = timGenerationHelper.resetTimStartTimeAndResubmitToOde(manualRefresh.getActiveTimIds());

        return resetSuccessful && exceptionTims.isEmpty() ? ResponseEntity.ok(true)
                : ResponseEntity.badRequest().body(false);
    }
}