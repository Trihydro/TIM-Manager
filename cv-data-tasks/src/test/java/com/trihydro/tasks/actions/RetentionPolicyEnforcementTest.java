package com.trihydro.tasks.actions;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.DriverAlertService;
import com.trihydro.library.service.HmiLogService;
import com.trihydro.library.service.StatusLogService;
import com.trihydro.library.service.TimService;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RetentionPolicyEnforcementTest {

    @Mock
    private DataTasksConfiguration mockConfig;
    @Mock
    private Utility mockUtility;
    @Mock
    private DriverAlertService mockDriverAlertService;
    @Mock
    private HmiLogService mockHmiLogService;
    @Mock
    private StatusLogService mockStatusLogService;
    @Mock
    private TimService mockTimService;

    @InjectMocks
    public RetentionPolicyEnforcement uut;

    @Test
    public void retentionPolicyEnforcement_runTest() {
        when(mockConfig.getRetention_removeTims()).thenReturn(true);
        when(mockConfig.getRetention_removeDa()).thenReturn(true);
        when(mockConfig.getRetention_removeHmi()).thenReturn(true);
        when(mockConfig.getRetention_removeStatusLogs()).thenReturn(true);


        uut.run();

        // assert services called
        verify(mockTimService).deleteOldTim();
        verify(mockDriverAlertService).deleteOldDriverAlerts();
        verify(mockHmiLogService).deleteOldHmiLogs();
        verify(mockStatusLogService).deleteOldStatusLogs();
    }
}