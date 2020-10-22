package com.trihydro.tasks.actions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.BsmService;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CleanupBsmsTest {
    @Mock
    private DataTasksConfiguration mockConfiguration;
    @Mock
    private BsmService mockBsmService;
    @Mock
    private Utility mockUtility;

    @Captor
    private ArgumentCaptor<List<String>> partitionNamesCaptor;

    @InjectMocks
    public CleanupBsms uut;

    @BeforeEach
    public void setup() {
        lenient().doReturn(1).when(mockConfiguration).getBsmRetentionPeriodDays();
    }

    @Test
    public void cleanupBsms_success() {
        // Arrange
        doReturn(true).when(mockBsmService).deleteOldBsm(any());

        // Act
        uut.run();

        // Assert
        verify(mockUtility).logWithDate("Running...", CleanupBsms.class);
        verify(mockUtility).logWithDate("Successfully removed old BSM data");
    }

    @Test
    public void cleanupBsms_FAIL() {
        // Arrange
        doReturn(false).when(mockBsmService).deleteOldBsm(any());

        // Act
        uut.run();

        // Assert
        verify(mockUtility).logWithDate("Running...", CleanupBsms.class);
        verify(mockUtility).logWithDate("Failed to remove old BSM data");
    }

    @Test
    public void cleanupBsms_handlesErrors() {
        // Arrange
        var exMessage = "exception";
        doThrow(new NullPointerException(exMessage)).when(mockBsmService).deleteOldBsm(1);

        // Act
        // We want to ensure no error propogates up. If it does, the runnable won't be
        // scheduled again until the application is restarted.
        uut.run();

        // Assert
        verify(mockUtility).logWithDate("Exception during BSM cleanup: exception");
    }
}