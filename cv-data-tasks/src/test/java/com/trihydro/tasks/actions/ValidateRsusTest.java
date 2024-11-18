package com.trihydro.tasks.actions;

import static com.trihydro.tasks.TestHelper.importJsonArray;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.RsuDataService;
import com.trihydro.library.service.RsuService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.helpers.EmailFormatter;
import com.trihydro.tasks.helpers.ExecutorFactory;
import com.trihydro.tasks.models.RsuValidationRecord;
import com.trihydro.tasks.models.RsuValidationResult;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
public class ValidateRsusTest {
    @Mock
    private DataTasksConfiguration mockConfig;
    @Mock
    private ActiveTimService mockActiveTimService;
    @Mock
    private RsuService mockRsuService;
    @Mock
    private RsuDataService mockRsuDataService;
    @Mock
    private OdeService mockOdeService;
    @Mock
    private TimGenerationHelper mockTimGenerationHelper;
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
    @Mock
    private Future<RsuValidationResult> thirdTask;

    @Captor
    private ArgumentCaptor<List<RsuValidationRecord>> rsuValidationRecords;
    @Captor
    private ArgumentCaptor<List<ValidateRsu>> rsuValidationTasks;
    @Captor
    private ArgumentCaptor<List<String>> unexpectedErrors;

    @InjectMocks
    private ValidateRsus uut;

    @BeforeEach
    public void setup() {
        lenient().when(mockConfig.getCvRestService()).thenReturn("cvRestServiceUrl");

        // Prevent delay before second validation attempt to keep unit tests performant
        uut.setDelaySecondValidation(false);
    }

    private void setupThreadpool() throws InterruptedException {
        // Configure mock threadpool behavior
        when(mockExecutorFactory.getFixedThreadPool(any(int.class))).thenReturn(mockExecutorService);
        when(mockExecutorService.awaitTermination(1, TimeUnit.SECONDS)).thenReturn(true);
        when(mockConfig.getRsuValThreadPoolSize()).thenReturn(1);
    }

    private void configureServiceReturns() {
        ActiveTim[] activeTims = importJsonArray("/activeTims_3.json", ActiveTim[].class);
        when(mockActiveTimService.getActiveRsuTims("cvRestServiceUrl")).thenReturn(Arrays.asList(activeTims));
    }

    private void configureRsuServiceReturns() {
        WydotRsu[] rsus = new WydotRsu[] { new WydotRsu(), new WydotRsu(), new WydotRsu() };
        rsus[0].setRsuTarget("0.0.0.0");
        rsus[1].setRsuTarget("0.0.0.1");
        rsus[2].setRsuTarget("0.0.0.2");

        when(mockRsuService.selectAll()).thenReturn(Arrays.asList(rsus));
    }

    @Test
    public void validateRsus_noRecords() throws MailException, MessagingException {
        // Arrange
        // Simulate when the service responds with no Active Tims
        when(mockActiveTimService.getActiveRsuTims(any())).thenReturn(new ArrayList<>());
        when(mockRsuService.selectAll()).thenReturn(new ArrayList<>());

        // Act
        uut.run();

        // Assert
        // If we're unable to find any RSUs to validate, we'll send an email to let us know
        // that there was a validation error.
        verify(mockMailHelper).SendEmail(any(), any(), any());
    }

    @Test
    public void validateRsus_unresponsiveActiveTimService() {
        // Arrange
        when(mockActiveTimService.getActiveRsuTims(any())).thenThrow(new RestClientException("timeout"));

        // Act (error should be handled in runnable)
        uut.run();

        // Assert
        verify(mockUtility).logWithDate(
                "Unable to validate RSUs - error occurred while fetching Database records from PROD:", ValidateRsus.class);
    }

    @Test
    public void validateRsus_noErrors()
            throws InterruptedException, ExecutionException, MailException, MessagingException {
        // Arrange
        setupThreadpool();
        configureServiceReturns(); // Service responds with with 4 Active Tims (across 2 RSUs)
        // 2 validation tasks generated, as a result. Simulate their responses.
        when(firstTask.get()).thenReturn(new RsuValidationResult());
        when(secondTask.get()).thenReturn(new RsuValidationResult());

        List<Future<RsuValidationResult>> results = Arrays.asList(firstTask, secondTask);
        doReturn(results).when(mockExecutorService).invokeAll(any(), any(Long.class), any(TimeUnit.class));

        // Act
        uut.run();

        // Assert
        verify(mockMailHelper, never()).SendEmail(any(), any(), any());
        verify(mockExecutorService).invokeAll(rsuValidationTasks.capture(), any(Long.class), any(TimeUnit.class));
        Assertions.assertEquals(2, rsuValidationTasks.getValue().size());
        Assertions.assertEquals("0.0.0.0", rsuValidationTasks.getValue().get(0).getIpv4Address());
        Assertions.assertEquals("0.0.0.1", rsuValidationTasks.getValue().get(1).getIpv4Address());
    }

    @Test
    public void validateRsus_success()
            throws InterruptedException, ExecutionException, MailException, MessagingException {
        // Arrange
        // - first RSU to be unresponsive
        // - second RSU to contain errors
        setupThreadpool();
        RsuValidationResult firstRsu = new RsuValidationResult();
        RsuValidationResult secondRsu = new RsuValidationResult();
        RsuValidationResult secondRsuSecondPass = new RsuValidationResult();

        firstRsu.setRsuUnresponsive(true);
        secondRsu.setUnaccountedForIndices(Arrays.asList(5));

        configureServiceReturns(); // Service responds with with 4 Active Tims (across 2 RSUs)
        // 2 validation tasks generated, as a result. Simulate their responses.
        when(firstTask.get()).thenReturn(firstRsu);
        when(secondTask.get()).thenReturn(secondRsu).thenReturn(secondRsuSecondPass);

        when(mockExecutorService.invokeAll(any(), any(Long.class), any(TimeUnit.class)))
                .thenAnswer(x -> Arrays.asList(firstTask, secondTask)).thenAnswer(x -> Arrays.asList(secondTask));

        // Act
        uut.run();

        // Assert
        verify(mockEmailFormatter).generateRsuSummaryEmail(rsuValidationRecords.capture(), unexpectedErrors.capture());
        verify(mockMailHelper).SendEmail(any(), any(), any());

        // The first RSU was unresponsive. Not an auto-correctable error so we only
        // tried validating it once.
        // The second RSU had an unaccounted for index, so we tried re-validating it
        // after attempting corrective action
        Assertions.assertEquals(1, rsuValidationRecords.getValue().get(0).getValidationResults().size());
        Assertions.assertEquals(2, rsuValidationRecords.getValue().get(1).getValidationResults().size());

        // We tried to fix the unaccounted for index error
        verify(mockOdeService).deleteTimFromRsu(any(), eq(5));

        Assertions
                .assertTrue(rsuValidationRecords.getValue().get(0).getValidationResults().get(0).getRsuUnresponsive());
        Assertions
                .assertFalse(rsuValidationRecords.getValue().get(1).getValidationResults().get(0).getRsuUnresponsive());
        Assertions.assertEquals(1,
                rsuValidationRecords.getValue().get(1).getValidationResults().get(0).getUnaccountedForIndices().size());
        Assertions.assertEquals(0, unexpectedErrors.getValue().size());
    }

    @Test
    public void validateRsus_unfinishedTasks()
            throws InterruptedException, ExecutionException, MailException, MessagingException {
        // Arrange
        // - first RSU to validate successfully
        // - second RSU to have been cancelled during validation (thread pool timeout)
        setupThreadpool();
        configureServiceReturns(); // Service responds with with 4 Active Tims (across 2 RSUs)
        // 2 validation tasks generated, as a result. Simulate their responses.
        when(firstTask.get()).thenReturn(new RsuValidationResult());
        when(secondTask.get()).thenThrow(new CancellationException());

        List<Future<RsuValidationResult>> results = Arrays.asList(firstTask, secondTask);
        doReturn(results).when(mockExecutorService).invokeAll(any(), any(Long.class), any(TimeUnit.class));

        // Act
        uut.run();

        // Assert
        verify(mockEmailFormatter).generateRsuSummaryEmail(rsuValidationRecords.capture(), unexpectedErrors.capture());
        verify(mockMailHelper).SendEmail(any(), any(), any());

        Assertions.assertEquals(0, unexpectedErrors.getValue().size());
        Assertions.assertNotNull(rsuValidationRecords.getValue().get(1).getError());
        Assertions.assertEquals("Error while validating RSU 0.0.0.1:\njava.util.concurrent.CancellationException",
                rsuValidationRecords.getValue().get(1).getError());
    }

    // Suppose we have at least one RSU that doesn't have an Active TIM.
    // This ensures that RSU will still be checked for unaccounted for indices
    @Test
    public void validateRsus_unmatchedRsu_noErrors()
            throws InterruptedException, ExecutionException, MailException, MessagingException {
        // Arrange
        // 0.0.0.0, 0.0.0.1 have Active TIMs
        // 0.0.0.2 doesn't have any Active TIMs

        setupThreadpool();
        configureServiceReturns();
        configureRsuServiceReturns();

        when(firstTask.get()).thenReturn(new RsuValidationResult());
        when(secondTask.get()).thenReturn(new RsuValidationResult());
        when(thirdTask.get()).thenReturn(new RsuValidationResult());

        List<Future<RsuValidationResult>> results = Arrays.asList(firstTask, secondTask, thirdTask);
        doReturn(results).when(mockExecutorService).invokeAll(any(), any(Long.class), any(TimeUnit.class));

        // Act
        uut.run();

        // Assert
        verify(mockMailHelper, never()).SendEmail(any(), any(), any());
        verify(mockRsuService).selectAll();

        verify(mockExecutorService).invokeAll(rsuValidationTasks.capture(), any(Long.class), any(TimeUnit.class));
        Assertions.assertEquals(3, rsuValidationTasks.getValue().size());
        Assertions.assertEquals("0.0.0.0", rsuValidationTasks.getValue().get(0).getIpv4Address());
        Assertions.assertEquals("0.0.0.1", rsuValidationTasks.getValue().get(1).getIpv4Address());
        Assertions.assertEquals("0.0.0.2", rsuValidationTasks.getValue().get(2).getIpv4Address());
    }

    @Test
    public void validateRsus_unmatchedRsus_unaccountedForIndices()
            throws InterruptedException, ExecutionException, MailException, MessagingException {
        // Arrange
        // In this scenario, there are no active TIMs
        // 0.0.0.0, 0.0.0.1 are error-free
        // 0.0.0.2 has unaccounted for indices
        setupThreadpool();
        configureRsuServiceReturns();

        when(firstTask.get()).thenReturn(new RsuValidationResult());
        when(secondTask.get()).thenReturn(new RsuValidationResult());

        RsuValidationResult lastRsu = new RsuValidationResult();
        lastRsu.setUnaccountedForIndices(Arrays.asList(2));
        when(thirdTask.get()).thenReturn(lastRsu);

        when(mockExecutorService.invokeAll(any(), any(Long.class), any(TimeUnit.class)))
                .thenAnswer(x -> Arrays.asList(firstTask, secondTask, thirdTask)).thenAnswer(x -> Arrays.asList(thirdTask));

        // Act
        uut.run();

        // Assert
        verify(mockEmailFormatter).generateRsuSummaryEmail(rsuValidationRecords.capture(), unexpectedErrors.capture());
        verify(mockMailHelper).SendEmail(any(), any(), any());

        var lastRsuResult = rsuValidationRecords.getValue().get(2);
        Assertions.assertEquals(2, lastRsuResult.getValidationResults().size());
        Assertions.assertEquals(1, lastRsuResult.getValidationResults().get(0).getUnaccountedForIndices().size());
        Assertions.assertEquals(2, lastRsuResult.getValidationResults().get(0).getUnaccountedForIndices().get(0));

        // We attempted to clear 0.0.0.2's unaccounted for indices
        verify(mockOdeService).deleteTimFromRsu(any(), eq(2));

        Assertions.assertEquals(0, unexpectedErrors.getValue().size());

        verify(mockExecutorService, times(2)).invokeAll(rsuValidationTasks.capture(), any(Long.class), any(TimeUnit.class));
        Assertions.assertEquals(3, rsuValidationTasks.getAllValues().get(0).size());
        Assertions.assertEquals(1, rsuValidationTasks.getAllValues().get(1).size());
    }

    @Test
    public void validateRsus_proceedsWithPartialValidation()
            throws InterruptedException, ExecutionException, MailException, MessagingException {
        // Arrange
        setupThreadpool();
        configureServiceReturns();

        // Error when attempting to get all RSUs
        when(mockRsuService.selectAll()).thenThrow(new RestClientException("timeout"));

        when(firstTask.get()).thenReturn(new RsuValidationResult());
        when(secondTask.get()).thenReturn(new RsuValidationResult());

        List<Future<RsuValidationResult>> results = Arrays.asList(firstTask, secondTask);
        doReturn(results).when(mockExecutorService).invokeAll(any(), any(Long.class), any(TimeUnit.class));

        // Act
        uut.run();

        // Assert
        verify(mockEmailFormatter).generateRsuSummaryEmail(rsuValidationRecords.capture(), unexpectedErrors.capture());
        verify(mockMailHelper).SendEmail(any(), any(), any());

        // 2 validation tasks were still performed. 2 RSUs (0.0.0.0 and 0.0.0.1) had
        // Active TIMs, and were found
        // during the first fetch
        verify(mockExecutorService).invokeAll(rsuValidationTasks.capture(), any(Long.class), any(TimeUnit.class));
        Assertions.assertEquals(2, rsuValidationTasks.getValue().size());

        Assertions.assertEquals(1, unexpectedErrors.getValue().size());
        Assertions.assertEquals(
                "Error occurred while fetching all RSUs - "
                        + "unable to validate any RSUs that don't have an existing, active TIM. Error:\n"
                        + "org.springframework.web.client.RestClientException: timeout",
                unexpectedErrors.getValue().get(0));
    }
}