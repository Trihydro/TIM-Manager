package com.trihydro.tasks.actions;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.service.ActiveTimHoldingService;
import com.trihydro.library.service.ActiveTimService;
import java.util.ArrayList;
import java.util.HashMap;
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
    public CleanupStaleActiveTimHoldingRecords(ActiveTimHoldingService activeTimHoldingService, ActiveTimService activeTimService) {
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
        List<ActiveTimHolding> currentRecords = retrieveAllActiveTimHoldingRecords();
        log.info("Retrieved {} active_tim_holding records", currentRecords.size());

        // If no stale records identified last run, this could be the first time the task runs, so add all active_tim_holding records to staleRecords set
        if (staleRecordsIdentifiedLastRun.isEmpty()) {
            log.info("No stale records identified last run. Adding {} active_tim_holding records to staleRecords set for next run", currentRecords.size());
            for (ActiveTimHolding record : currentRecords) {
                staleRecordsIdentifiedLastRun.add(record.getActiveTimHoldingId());
            }
            return;
        }

        // Separate likely stale active_tim_holding records
        List<ActiveTimHolding> likelyStaleRecords = new ArrayList<>();
        List<ActiveTimHolding> newRecords = new ArrayList<>();
        for (ActiveTimHolding record : currentRecords) {
            if (staleRecordsIdentifiedLastRun.contains(record.getActiveTimHoldingId())) {
                likelyStaleRecords.add(record);
            } else {
                newRecords.add(record);
            }
        }
        log.info("Identified {} likely stale active_tim_holding records", likelyStaleRecords.size());

        // Check for active_tim records
        List<ActiveTim> activeTims = retrieveAllActiveTimRecords();
        HashMap<String, List<Long>> activeTimIdsByClientId = mapActiveTimIdsByClientId(activeTims);
        List<Long> activeTimIdsToDelete = new ArrayList<>();
        for (ActiveTimHolding record : likelyStaleRecords) {
            List<Long> activeTimIds = activeTimIdsByClientId.get(record.getClientId());
            if (activeTimIds == null) {
                continue;
            }
            activeTimIdsToDelete.addAll(activeTimIds);
            // TODO: Delete only if the failed update was to expire the active TIM.
            // TODO: Consider re-submitting the active TIM if the failed update was not meant to expire it.
        }
        removeActiveTimRecords(activeTimIdsToDelete); // active_tim records are no longer up-to-date
        log.info("Deleted corresponding active_tim records with ids: ({}), which were outdated (the presence of stale active_tim_holding records indicates a failure to update).", activeTimIdsToDelete);

        // Delete likely stale active_tim_holding records
        for (ActiveTimHolding record : likelyStaleRecords) {
            removeActiveTimHoldingRecord(record);
        }
        log.info("Deleted {} likely stale active_tim_holding records", likelyStaleRecords.size());

        // Clear staleRecords set
        log.info("Clearing staleRecords set...");
        staleRecordsIdentifiedLastRun.clear();

        // Record for future runs
        for (ActiveTimHolding record : newRecords) {
            staleRecordsIdentifiedLastRun.add(record.getActiveTimHoldingId());
        }
        log.info("Added {} active_tim_holding records to staleRecords set for next run", newRecords.size());
    }

    private static HashMap<String, List<Long>> mapActiveTimIdsByClientId(List<ActiveTim> activeTims) {
        HashMap<String, List<Long>> activeTimIdsByClientId = new HashMap<>();
        for (ActiveTim activeTim : activeTims) {
            if (!activeTimIdsByClientId.containsKey(activeTim.getClientId())) {
                activeTimIdsByClientId.put(activeTim.getClientId(), new ArrayList<>());
            }
            activeTimIdsByClientId.get(activeTim.getClientId()).add(activeTim.getActiveTimId());
        }
        return activeTimIdsByClientId;
    }

    private List<ActiveTimHolding> retrieveAllActiveTimHoldingRecords() {
        try {
            return activeTimHoldingService.getAllRecords();
        } catch (Exception e) {
            log.error("Failed to retrieve all active_tim_holding records. Is the cv-data-controller service running?", e);
        }
        return new ArrayList<>();
    }

    private List<ActiveTim> retrieveAllActiveTimRecords() {
        try {
            return activeTimService.getAllRecords();
        } catch (Exception e) {
            log.error("Failed to retrieve all active_tim records. Is the cv-data-controller service running?", e);
        }
        return new ArrayList<>();
    }

    private void removeActiveTimRecords(List<Long> activeTimIds) {
        try {
            boolean success = activeTimService.deleteActiveTimsById(activeTimIds);
            if (!success) {
                log.error("Failed to delete active_tim records with ids: {}", activeTimIds);
            }
        } catch (Exception e) {
            log.error("Failed to delete active_tim record with ids: {}. Is the cv-data-controller service running?", activeTimIds, e);
        }
    }

    private void removeActiveTimHoldingRecord(ActiveTimHolding record) {
        try {
            boolean success = activeTimHoldingService.deleteActiveTimHolding(record.getActiveTimHoldingId());
            if (!success) {
                log.error("Failed to delete active_tim_holding record with id: {}", record.getActiveTimHoldingId());
            }
        } catch (Exception e) {
            log.error("Failed to delete active_tim_holding record with id: {}. Is the cv-data-controller service running?", record.getActiveTimHoldingId(), e);
        }
    }

    /**
     * For testing purposes, this method returns the staleRecordsIdentifiedLastRun set.
     */
    protected Set<Long> getStaleRecordsIdentifiedLastRun() {
        return staleRecordsIdentifiedLastRun;
    }

    /**
     * For testing purposes, this method sets the staleRecordsIdentifiedLastRun set.
     */
    protected void setStaleRecordsIdentifiedLastRun(Set<Long> staleRecordsIdentifiedLastRun) {
        this.staleRecordsIdentifiedLastRun.clear();
        this.staleRecordsIdentifiedLastRun.addAll(staleRecordsIdentifiedLastRun);
    }
}