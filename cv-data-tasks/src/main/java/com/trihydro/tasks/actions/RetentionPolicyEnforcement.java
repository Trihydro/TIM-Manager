package com.trihydro.tasks.actions;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.DriverAlertService;
import com.trihydro.library.service.HmiLogService;
import com.trihydro.library.service.StatusLogService;
import com.trihydro.library.service.TimService;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RetentionPolicyEnforcement implements Runnable {
    private Utility utility;
    private DriverAlertService driverAlertService;
    private HmiLogService hmiLogService;
    private StatusLogService statusLogService;
    private TimService timService;
    private DataTasksConfiguration config;

    @Autowired
    public void InjectDependencies(Utility _utility, DriverAlertService _driverAlertService,
            HmiLogService _hmiLogService, StatusLogService _statusLogService, TimService _timService,
            DataTasksConfiguration _config) {
        this.utility = _utility;
        this.driverAlertService = _driverAlertService;
        this.hmiLogService = _hmiLogService;
        this.statusLogService = _statusLogService;
        this.timService = _timService;
        this.config = _config;
    }

    public void run() {
        utility.logWithDate("Running...", this.getClass());

        try {
            // delete all older than a month:
            if (config.getRetention_removeTims()) {
                // TIM
                timService.deleteOldTim();
            }

            if (config.getRetention_removeDa()) {
                // Driver Alerts
                driverAlertService.deleteOldDriverAlerts();
            }

            if (config.getRetention_removeHmi()) {
                // HMI Logs
                hmiLogService.deleteOldHmiLogs();
            }

            if (config.getRetention_removeStatusLogs()) {
                // Status Log
                statusLogService.deleteOldStatusLogs();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }
    }

}