package com.trihydro.tasks.actions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.service.ActiveTimHoldingService;
import com.trihydro.library.service.ActiveTimService;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class CleanupStaleActiveTimHoldingRecordsTest {

    @Mock
    ActiveTimHoldingService activeTimHoldingService;

    @Mock
    ActiveTimService activeTimService;

    @BeforeEach
    void setUp() {
        activeTimHoldingService = mock(ActiveTimHoldingService.class);
        activeTimService = mock(ActiveTimService.class);
    }

    /**
     * Test to verify that the initial run adds all records to the staleRecords set.
     */
    @Test
    void run_InitialRun_ShouldAddAllRecordsToStaleRecordsSet() {
        // prepare
        ActiveTimHolding ath1 = new ActiveTimHolding();
        ath1.setActiveTimHoldingId(1L);
        ActiveTimHolding ath2 = new ActiveTimHolding();
        ath2.setActiveTimHoldingId(2L);
        when(activeTimHoldingService.getAllRecords()).thenReturn(Arrays.asList(ath1, ath2));
        CleanupStaleActiveTimHoldingRecords cleanupStaleActiveTimHoldingRecords = new CleanupStaleActiveTimHoldingRecords(activeTimHoldingService, activeTimService);

        // execute
        cleanupStaleActiveTimHoldingRecords.run();

        // verify
        assertEquals(2, cleanupStaleActiveTimHoldingRecords.getStaleRecordsIdentifiedLastRun().size());
        verifyNoInteractions(activeTimService);
    }

    /**
     * Test to verify that subsequent runs with no matching ActiveTims delete all stale records.
     */
    @Test
    void run_SubsequentRun_NoMatchingActiveTims_ShouldDeleteAllStaleRecords() {
        // prepare
        ActiveTimHolding ath1 = new ActiveTimHolding();
        ath1.setActiveTimHoldingId(1L);
        ActiveTimHolding ath2 = new ActiveTimHolding();
        ath2.setActiveTimHoldingId(2L);
        ActiveTimHolding ath3 = new ActiveTimHolding();
        ath3.setActiveTimHoldingId(3L);
        when(activeTimHoldingService.getAllRecords()).thenReturn(Arrays.asList(ath1, ath2, ath3));
        when(activeTimService.getAllRecords()).thenReturn(List.of());
        CleanupStaleActiveTimHoldingRecords cleanupStaleActiveTimHoldingRecords = new CleanupStaleActiveTimHoldingRecords(activeTimHoldingService, activeTimService);
        cleanupStaleActiveTimHoldingRecords.setStaleRecordsIdentifiedLastRun(Set.of(1L, 2L)); // set stale records from previous run

        // execute
        cleanupStaleActiveTimHoldingRecords.run();

        // verify
        assertEquals(1, cleanupStaleActiveTimHoldingRecords.getStaleRecordsIdentifiedLastRun().size());
        verify(activeTimService).getAllRecords();
        verify(activeTimHoldingService).deleteActiveTimHolding(1L);
        verify(activeTimHoldingService).deleteActiveTimHolding(2L);
    }

    /**
     * Test to verify that subsequent runs with matching ActiveTims delete stale records and ActiveTims.
     */
    @Test
    void run_SubsequentRun_WithMatchingActiveTims_ShouldDeleteStaleRecordsAndActiveTims() {
        // prepare
        ActiveTimHolding ath1 = new ActiveTimHolding();
        ath1.setActiveTimHoldingId(1L);
        ath1.setClientId("test1");
        ActiveTimHolding ath2 = new ActiveTimHolding();
        ath2.setActiveTimHoldingId(2L);
        ath2.setClientId("test2");
        when(activeTimHoldingService.getAllRecords()).thenReturn(Arrays.asList(ath1, ath2));
        ActiveTim activeTim = new ActiveTim();
        activeTim.setActiveTimId(37L);
        activeTim.setClientId("test1");
        when(activeTimService.getAllRecords()).thenReturn(List.of(activeTim));
        CleanupStaleActiveTimHoldingRecords cleanupStaleActiveTimHoldingRecords = new CleanupStaleActiveTimHoldingRecords(activeTimHoldingService, activeTimService);
        cleanupStaleActiveTimHoldingRecords.setStaleRecordsIdentifiedLastRun(Set.of(1L, 2L)); // set stale records from previous run

        // execute
        cleanupStaleActiveTimHoldingRecords.run();

        // verify
        assertEquals(0, cleanupStaleActiveTimHoldingRecords.getStaleRecordsIdentifiedLastRun().size());
        verify(activeTimService).getAllRecords();
        verify(activeTimHoldingService).deleteActiveTimHolding(1L);
        verify(activeTimHoldingService).deleteActiveTimHolding(2L);
        verify(activeTimService).deleteActiveTimsById(List.of(37L));
    }

    /**
     * Test to verify that the run method handles database connection failures gracefully.
     */
    @Test
    void run_WhenDatabaseConnectionFails_ShouldHandleGracefully() {
        // prepare
        when(activeTimHoldingService.getAllRecords()).thenThrow(new RuntimeException());
        CleanupStaleActiveTimHoldingRecords cleanupStaleActiveTimHoldingRecords = new CleanupStaleActiveTimHoldingRecords(activeTimHoldingService, activeTimService);

        // execute
        cleanupStaleActiveTimHoldingRecords.run();

        // verify
        verifyNoInteractions(activeTimService);
    }

    /**
     * Test to verify that the run method handles an empty database gracefully.
     */
    @Test
    void run_WhenDatabaseIsEmpty_ShouldHandleGracefully() {
        // prepare
        when(activeTimHoldingService.getAllRecords()).thenReturn(List.of());
        CleanupStaleActiveTimHoldingRecords cleanupStaleActiveTimHoldingRecords = new CleanupStaleActiveTimHoldingRecords(activeTimHoldingService, activeTimService);

        // execute
        cleanupStaleActiveTimHoldingRecords.run();

        // verify
        verifyNoInteractions(activeTimService);
    }
}