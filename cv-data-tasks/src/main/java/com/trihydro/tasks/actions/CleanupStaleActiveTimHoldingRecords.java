package com.trihydro.tasks.actions;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.service.ActiveTimHoldingService;
import com.trihydro.library.service.ActiveTimService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CleanupStaleActiveTimHoldingRecords implements Runnable {

    private final ActiveTimHoldingService activeTimHoldingService;
    private final ActiveTimService activeTimService;

    // Set of likely stale records identified by active tim holding id (this will be updated at the end of each run)
    private final Set<Long> staleRecordsIdentifiedLastRun = new HashSet<>();

    @Autowired
    public CleanupStaleActiveTimHoldingRecords(ActiveTimHoldingService activeTimHoldingService,
        ActiveTimService activeTimService) {
        this.activeTimHoldingService = activeTimHoldingService;
        this.activeTimService = activeTimService;
    }

    /**
     * The run method is executed periodically to clean up stale ActiveTimHolding records.
     * It performs the following steps:
     * 1. Retrieves all active_tim_holding records from the database.
     * 2. Identifies likely stale active_tim_holding records.
     * 3. Checks for active_tim records with the same client_id and deletes them.
     * 4. Deletes likely stale active_tim_holding records.
     * 5. Clears the staleRecords set.
     * 6. Adds remaining active_tim_holding records to the staleRecords set.
     */
    @Override
    public void run() {
        log.info("Running...");

        // Retrieve all active_tim_holding records
        log.info("Retrieving all active_tim_holding records...");
        List<ActiveTimHolding> currentRecords = retrieveAllActiveTimHoldingRecords();

        // If no stale records identified last run, this could be the first time the task runs, so add all active_tim_holding records to staleRecords set
        if (staleRecordsIdentifiedLastRun.isEmpty()) {
            log.info("No stale records identified last run. Adding all active_tim_holding records to staleRecords set...");
            for (ActiveTimHolding record : currentRecords) {
                staleRecordsIdentifiedLastRun.add(record.getActiveTimHoldingId());
            }
            return;
        }

        // Separate likely stale active_tim_holding records
        log.info("Identifying likely stale active_tim_holding records...");
        List<ActiveTimHolding> likelyStaleRecords = new ArrayList<>();
        List<ActiveTimHolding> newRecords = new ArrayList<>();
        for (ActiveTimHolding record : currentRecords) {
            if (staleRecordsIdentifiedLastRun.contains(record.getActiveTimHoldingId())) {
                likelyStaleRecords.add(record);
            } else {
                newRecords.add(record);
            }
        }

        // Check for active_tim records
        log.info("Checking for active_tim records with same client_id...");
        List<ActiveTim> activeTims = retrieveAllActiveTimRecords();
        for (ActiveTimHolding record : likelyStaleRecords) {
            for (ActiveTim activeTim : activeTims) {
                if (record.getClientId().equals(activeTim.getClientId())) {
                    log.info("Deleting active_tim record with id: {}", activeTim.getActiveTimId());
                    removeActiveTimRecord(activeTim);
                }
            }
        }

        // Delete likely stale active_tim_holding records
        log.info("Deleting likely stale active_tim_holding records...");
        for (ActiveTimHolding record : likelyStaleRecords) {
            log.info("Deleting active_tim_holding record with id: {}",
                record.getActiveTimHoldingId());
            removeActiveTimHoldingRecord(record);
        }

        // Clear staleRecords set
        log.info("Clearing staleRecords set...");
        staleRecordsIdentifiedLastRun.clear();

        // Record for future runs
        log.info("Adding remaining active_tim_holding records to staleRecords set...");
        for (ActiveTimHolding record : newRecords) {
            staleRecordsIdentifiedLastRun.add(record.getActiveTimHoldingId());
        }
    }

    private List<ActiveTimHolding> retrieveAllActiveTimHoldingRecords() {
        return activeTimHoldingService.getAllRecords();
    }

    private List<ActiveTim> retrieveAllActiveTimRecords() {
        return activeTimService.getAllRecords();
    }

    private void removeActiveTimRecord(ActiveTim activeTim) {
        boolean success = activeTimService.deleteActiveTim(activeTim.getActiveTimId());
        if (!success) {
            log.error("Failed to delete active_tim record with id: {}", activeTim.getActiveTimId());
        }
    }

    private void removeActiveTimHoldingRecord(ActiveTimHolding record) {
        boolean success = activeTimHoldingService.deleteActiveTimHolding(record.getActiveTimHoldingId());
        if (!success) {
            log.error("Failed to delete active_tim_holding record with id: {}", record.getActiveTimHoldingId());
        }
    }
}