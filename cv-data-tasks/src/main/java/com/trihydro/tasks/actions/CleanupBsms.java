package com.trihydro.tasks.actions;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.BsmService;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CleanupBsms implements Runnable {
    private DataTasksConfiguration configuration;
    private BsmService bsmService;
    private Utility utility;

    @Autowired
    public void InjectDependencies(DataTasksConfiguration configuration, BsmService _bsmService, Utility utility) {
        this.configuration = configuration;
        this.bsmService = _bsmService;
        this.utility = utility;
    }

    @Override
    public void run() {
        utility.logWithDate("Running...", this.getClass());

        try {
            // delete old BSM records based on retention days
            // this calls out to the BsmController and fetches the largest bsm_core_id 
            // for the retention period. It then deletes all bsm_part2_vse, bsm_part2_suve
            // bsm_part2_spve, bsm_core_data less than or equal to the id
            var success = bsmService.deleteOldBsm(configuration.getBsmRetentionPeriodDays());
            if (success) {
                utility.logWithDate("Successfully removed old BSM data");
            } else {
                utility.logWithDate("Either no old BSM data was found to remove, or the data failed to delete. Exceptions are logged separately.");
            }
        } catch (Exception e) {
            utility.logWithDate("Exception during BSM cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}