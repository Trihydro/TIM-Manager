package com.trihydro.tasks.actions;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.BsmService;
import com.trihydro.library.service.DriverAlertService;
import com.trihydro.library.service.HmiLogService;
import com.trihydro.library.service.StatusLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RetentionPolicyEnforcement implements Runnable {
    private Utility utility;
    private BsmService bsmService;
    DriverAlertService driverAlertService;
    HmiLogService hmiLogService;
    StatusLogService statusLogService;

    @Autowired
    public void InjectDependencies(Utility _utility, BsmService _bsmService, DriverAlertService _driverAlertService,
            HmiLogService _hmiLogService, StatusLogService _statusLogService) {
        this.utility = _utility;
        this.driverAlertService = _driverAlertService;
        this.hmiLogService = _hmiLogService;
        this.statusLogService = _statusLogService;
    }

    public void run() {
        utility.logWithDate("Running...", this.getClass());

        try {
            // TODO: delete all older than a month:
            // TIM
            // TIM_RSU
            // BSM
            bsmService.deleteOldBsm();
            // Driver Alerts
            driverAlertService.deleteOldDriverAlerts();
            // HMI Logs
            hmiLogService.deleteOldHmiLogs();
            // Status Log
            statusLogService.deleteOldStatusLogs();

        } catch (Exception e) {
            e.printStackTrace();
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }
    }

}