package com.trihydro.tasks.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.BsmCoreDataPartition;
import com.trihydro.library.service.UtilityService;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
public class CleanupBsmsTest {
    @Mock
    private DataTasksConfiguration mockConfiguration;
    @Mock
    private UtilityService mockUtilityService;
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
        // 1 partition, sys_1, which is 25 hours old (1 hour outside retention period)
        when(mockUtilityService.getBsmCoreDataPartitions()).thenReturn(Arrays.asList(new BsmCoreDataPartition[] {
                new BsmCoreDataPartition("sys_1", new Date(Instant.now().toEpochMilli() - 1000 * 60 * 60 * 25)) }));

        // Act
        uut.run();

        // Assert
        verify(mockUtility).logWithDate("Removing 1 partitions from BSM_CORE_DATA.", CleanupBsms.class);
        verify(mockUtilityService).dropBsmPartitions(partitionNamesCaptor.capture());

        List<String> names = partitionNamesCaptor.getValue();
        assertEquals(1, names.size());
        assertEquals("sys_1", names.get(0));
    }

    @Test
    public void cleanupBsms_noPartitions() {
        // Arrange
        when(mockUtilityService.getBsmCoreDataPartitions()).thenReturn(Arrays.asList(new BsmCoreDataPartition[0]));

        // Act
        uut.run();

        // Assert
        verify(mockUtility, times(0)).logWithDate(any(), any());
        verify(mockUtilityService, times(0)).dropBsmPartitions(any());
    }

    @Test
    public void cleanupBsms_noOldPartitions() {
        // Arrange
        // 1 partition, sys_1, which is brand new
        when(mockUtilityService.getBsmCoreDataPartitions()).thenReturn(
                Arrays.asList(new BsmCoreDataPartition[] { new BsmCoreDataPartition("sys_1", new Date()) }));

        // Act
        uut.run();

        // Assert
        verify(mockUtility, times(0)).logWithDate(any(), any());
        verify(mockUtilityService, times(0)).dropBsmPartitions(any());
    }

    @Test
    public void cleanupBsms_handlesErrors() {
        // Arrange
        when(mockUtilityService.getBsmCoreDataPartitions())
                .thenThrow(new RestClientException("something went wrong..."));

        // Act
        // We want to ensure no error propogates up. If it does, the runnable won't be
        // scheduled again until the application is restarted.
        uut.run();
    }
}