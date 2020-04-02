package com.trihydro.tasks.actions;

import static com.trihydro.tasks.TestHelper.importJsonArray;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RsuDataService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.helpers.EmailFormatter;
import com.trihydro.tasks.helpers.ExecutorFactory;
import com.trihydro.tasks.models.RsuValidationResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailException;
import org.springframework.web.client.RestClientException;

public class ValidateRsusTest {
    @Mock
    private DataTasksConfiguration mockConfig;
    @Mock
    private ActiveTimService mockActiveTimService;
    @Mock
    private RsuDataService mockRsuDataService;
    @Mock
    private ExecutorFactory mockExecutorFactory;
    @Mock
    private EmailFormatter mockEmailFormatter;
    @Mock
    private EmailHelper mockMailHelper;
    @Mock
    private Utility mockUtility;

    @Mock
    private ExecutorService mockExecutorService;
    @Mock
    private Future<RsuValidationResult> firstTask;
    @Mock
    private Future<RsuValidationResult> secondTask;

    @Captor
    private ArgumentCaptor<List<String>> unresponsiveRsus;
    @Captor
    private ArgumentCaptor<List<RsuValidationResult>> rsusWithErrors;
    @Captor
    private ArgumentCaptor<List<String>> unexpectedErrors;

    @InjectMocks
    private ValidateRsus uut;

    @Before
    public void setup() throws InterruptedException {
        MockitoAnnotations.initMocks(this);

        // Configure mock threadpool behavior
        when(mockExecutorFactory.getFixedThreadPool(any(int.class))).thenReturn(mockExecutorService);
        when(mockExecutorService.awaitTermination(1, TimeUnit.SECONDS)).thenReturn(true);
        when(mockConfig.getRsuValThreadPoolSize()).thenReturn(1);

        when(mockConfig.getCvRestServiceDev()).thenReturn("devUrl");
        when(mockConfig.getCvRestServiceProd()).thenReturn("prodUrl");
    }

    private void configureServiceReturns() {
        // Split up the active tims so it seems like we have 2 coming from dev and 2
        // from prod
        ActiveTim[] activeTims = importJsonArray("/activeTims_3.json", ActiveTim[].class);
        when(mockActiveTimService.getActiveRsuTims("devUrl")).thenReturn(Arrays.asList(activeTims[0], activeTims[1]));
        when(mockActiveTimService.getActiveRsuTims("prodUrl")).thenReturn(Arrays.asList(activeTims[2], activeTims[3]));
    }

    @Test
    public void validateRsus_noRecords() throws MailException, MessagingException {
        // Arrange
        // Simulate when the service responds with no Active Tims
        when(mockActiveTimService.getActiveRsuTims(any())).thenReturn(new ArrayList<>());

        // Act
        uut.run();

        // Assert
        verify(mockMailHelper, never()).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateRsus_unresponsiveActiveTimService() {
        // Arrange
        when(mockActiveTimService.getActiveRsuTims(any())).thenThrow(new RestClientException("timeout"));

        // Act (error should be handled in runnable)
        uut.run();

        // Assert
        verify(mockUtility)
                .logWithDate("Unable to validate RSUs - error occurred while fetching Oracle records from DEV:");
    }

    @Test
    public void validateRsus_noErrors()
            throws InterruptedException, ExecutionException, MailException, MessagingException {
        // Arrange
        configureServiceReturns(); // Service responds with with 4 Active Tims (across 2 RSUs)
        // 2 validation tasks generated, as a result. Simulate their responses.
        when(firstTask.get()).thenReturn(new RsuValidationResult("0.0.0.0"));
        when(secondTask.get()).thenReturn(new RsuValidationResult("0.0.0.1"));

        List<Future<RsuValidationResult>> results = Arrays.asList(firstTask, secondTask);
        doReturn(results).when(mockExecutorService).invokeAll(any(), any(Long.class), any(TimeUnit.class));

        // Act
        uut.run();

        // Assert
        verify(mockMailHelper, never()).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateRsus_success()
            throws InterruptedException, ExecutionException, MailException, MessagingException {
        // Arrange
        // - first RSU to be unresponsive
        // - second RSU to contain errors
        RsuValidationResult firstRsu = new RsuValidationResult("0.0.0.0");
        RsuValidationResult secondRsu = new RsuValidationResult("0.0.0.1");
        firstRsu.setRsuUnresponsive(true);
        secondRsu.setUnaccountedForIndices(Arrays.asList(5));

        configureServiceReturns(); // Service responds with with 4 Active Tims (across 2 RSUs)
        // 2 validation tasks generated, as a result. Simulate their responses.
        when(firstTask.get()).thenReturn(firstRsu);
        when(secondTask.get()).thenReturn(secondRsu);

        List<Future<RsuValidationResult>> results = Arrays.asList(firstTask, secondTask);
        doReturn(results).when(mockExecutorService).invokeAll(any(), any(Long.class), any(TimeUnit.class));

        // Act
        uut.run();

        // Assert
        verify(mockEmailFormatter).generateRsuSummaryEmail(unresponsiveRsus.capture(), rsusWithErrors.capture(),
                unexpectedErrors.capture());
        verify(mockMailHelper).SendEmail(any(), any(), any(), any(), any(), any(), any());

        assertEquals(1, unresponsiveRsus.getValue().size());
        assertEquals(1, rsusWithErrors.getValue().size());
        assertEquals(0, unexpectedErrors.getValue().size());
    }

    @Test
    public void validateRsus_unfinishedTasks()
            throws InterruptedException, ExecutionException, MailException, MessagingException {
        // Arrange
        // - first RSU to validate successfully
        // - second RSU to have been cancelled during validation (thread pool timeout)
        configureServiceReturns(); // Service responds with with 4 Active Tims (across 2 RSUs)
        // 2 validation tasks generated, as a result. Simulate their responses.
        when(firstTask.get()).thenReturn(new RsuValidationResult("0.0.0.0"));
        when(secondTask.get()).thenThrow(new CancellationException());

        List<Future<RsuValidationResult>> results = Arrays.asList(firstTask, secondTask);
        doReturn(results).when(mockExecutorService).invokeAll(any(), any(Long.class), any(TimeUnit.class));

        // Act
        uut.run();

        // Assert
        verify(mockEmailFormatter).generateRsuSummaryEmail(unresponsiveRsus.capture(), rsusWithErrors.capture(),
                unexpectedErrors.capture());
        verify(mockMailHelper).SendEmail(any(), any(), any(), any(), any(), any(), any());

        assertEquals(0, unresponsiveRsus.getValue().size());
        assertEquals(0, rsusWithErrors.getValue().size());
        assertEquals(1, unexpectedErrors.getValue().size());
        assertEquals("0.0.0.1: java.util.concurrent.CancellationException - null", unexpectedErrors.getValue().get(0));
    }
}