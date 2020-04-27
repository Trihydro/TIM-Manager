package com.trihydro.tasks.actions;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.BsmService;
import com.trihydro.library.service.DriverAlertService;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RetentionPolicyEnforcement implements Runnable {

    private DataTasksConfiguration configuration;
    private Utility utility;
    private BsmService bsmService;
    DriverAlertService driverAlertService;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration configuration, Utility _utility, BsmService _bsmService,
    DriverAlertService _driverAlertService) {
        this.configuration = configuration;
        this.utility = _utility;
        this.driverAlertService = _driverAlertService;
    }

    public void run() {
        utility.logWithDate("Running...", this.getClass());

        try {
            // TODO: delete all older than a month:
            // TIM
            // BSM
            bsmService.deleteOldBsm();
            // Driver Alerts
            driverAlertService.deleteOldDriverAlerts();
            // HMI Logs
            // Status Log
            // TIM_RSU

        } catch (Exception e) {
            e.printStackTrace();
            // don't rethrow error, or the task won't be reran until the service is
            // restarted.
        }
    }

}