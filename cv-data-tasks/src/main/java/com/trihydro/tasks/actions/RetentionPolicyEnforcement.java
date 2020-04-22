package com.trihydro.tasks.actions;

import com.trihydro.library.helpers.Utility;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RetentionPolicyEnforcement {

    private DataTasksConfiguration configuration;
    private Utility utility;
    // private ActiveTimService activeTimService;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration configuration, Utility _utility) {
        this.configuration = configuration;
        utility = _utility;
    }

    public void run() {
        utility.logWithDate("Running...", this.getClass());

        try {
            // TODO: delete all older than a month:
            // TIM
            // BSM
            // Driver Alerts
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