package com.trihydro.tasks.actions;

import static com.trihydro.tasks.TestHelper.importJsonArray;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

import com.google.gson.Gson;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.GsonFactory;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TmddItisCode;
import com.trihydro.library.model.tmdd.FullEventUpdate;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.TmddService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.helpers.EmailFormatter;
import com.trihydro.tasks.helpers.IdNormalizer;
import com.trihydro.tasks.models.ActiveTimError;
import com.trihydro.tasks.models.ActiveTimValidationResult;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.web.client.RestClientException;

@RunWith(StrictStubs.class)
public class ValidateTmddTest {
    @Mock
    private TmddService mockTmddService;

    @Mock
    private ActiveTimService mockActiveTimService;

    @Mock
    private ItisCodeService mockItisCodeService;

    @Mock
    private Utility mockUtility;

    @Spy
    private IdNormalizer spyIdNormalizer;

    @Mock
    private EmailHelper mockEmailHelper;

    @Mock
    private DataTasksConfiguration mockConfig;

    @Mock
    private EmailFormatter mockEmailFormatter;

    @Captor
    private ArgumentCaptor<List<ActiveTim>> unableToVerifyCaptor;

    @Captor
    private ArgumentCaptor<List<ActiveTimValidationResult>> validationResultsCaptor;

    @Captor
    private ArgumentCaptor<String> logMessageCaptor;

    @InjectMocks
    ValidateTmdd uut;

    @Test
    public void validateTmdd_run_noRecords() throws Exception {
        // Act
        uut.run();

        // Assert
        verify(mockTmddService).getTmddEvents();
        verify(mockActiveTimService).getActiveTimsWithItisCodes();

        // Assert (no email sent)
        verify(mockEmailHelper, times(0)).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateTmdd_run_noErrors() throws Exception {
        // Show that we can handle validating multiple Active TIMs of different types.
        // Arrange
        Gson tmddDeserializer = new GsonFactory().getTmddDeserializer();

        ActiveTim[] activeTims = importJsonArray("/activeTims_4.json", ActiveTim[].class);
        FullEventUpdate[] feus = importJsonArray("/feus_1.json", FullEventUpdate[].class, tmddDeserializer);
        TmddItisCode[] itisCodes = importJsonArray("/tmdd_itis_codes.json", TmddItisCode[].class);

        when(mockTmddService.getTmddEvents()).thenReturn(Arrays.asList(feus));
        when(mockActiveTimService.getActiveTimsWithItisCodes()).thenReturn(Arrays.asList(activeTims));
        when(mockItisCodeService.selectAllTmddItisCodes()).thenReturn(Arrays.asList(itisCodes));

        // Act
        uut.run();

        // Assert
        // Service methods called
        verify(mockTmddService).getTmddEvents();
        verify(mockActiveTimService).getActiveTimsWithItisCodes();
        verify(mockItisCodeService).selectAllTmddItisCodes();

        // No email sent
        verify(mockEmailHelper, times(0)).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateTmdd_run_validationErrors() throws Exception {
        // Arrange
        Gson tmddDeserializer = new GsonFactory().getTmddDeserializer();

        ActiveTim[] activeTims = importJsonArray("/activeTims_5.json", ActiveTim[].class);
        FullEventUpdate[] feus = importJsonArray("/feus_2.json", FullEventUpdate[].class, tmddDeserializer);
        TmddItisCode[] itisCodes = importJsonArray("/tmdd_itis_codes.json", TmddItisCode[].class);

        when(mockTmddService.getTmddEvents()).thenReturn(Arrays.asList(feus));
        when(mockActiveTimService.getActiveTimsWithItisCodes()).thenReturn(Arrays.asList(activeTims));
        when(mockItisCodeService.selectAllTmddItisCodes()).thenReturn(Arrays.asList(itisCodes));

        // Act
        uut.run();

        // Assert
        // Service methods called
        verify(mockTmddService).getTmddEvents();
        verify(mockActiveTimService).getActiveTimsWithItisCodes();
        verify(mockItisCodeService).selectAllTmddItisCodes();

        // Validation summary email generated
        verify(mockEmailFormatter).generateTmddSummaryEmail(unableToVerifyCaptor.capture(),
                validationResultsCaptor.capture());

        List<ActiveTim> unableToVerify = unableToVerifyCaptor.getValue();
        List<ActiveTimValidationResult> validationResults = validationResultsCaptor.getValue();

        // Check validation results
        assertEquals(0, unableToVerify.size());
        assertEquals(1, validationResults.size());
        assertEquals(1l, (long) validationResults.get(0).getActiveTim().getActiveTimId());
        assertEquals(5, validationResults.get(0).getErrors().size());

        // Check errors individually
        List<ActiveTimError> errors = validationResults.get(0).getErrors();
        assertEquals("Start Time", errors.get(0).getName());
        assertEquals("2020-04-27 10:00:00", errors.get(0).getTimValue());
        assertEquals("2020-04-27 09:43:00", errors.get(0).getTmddValue());

        assertEquals("End Time", errors.get(1).getName());
        assertEquals("2020-04-27 11:00:00", errors.get(1).getTimValue());
        assertEquals(null, errors.get(1).getTmddValue());

        assertEquals("Start Point", errors.get(2).getName());
        assertEquals("{ lat: 42.750000, lon: -110.940000 }", errors.get(2).getTimValue());
        assertEquals("{ lat: 42.739996, lon: -110.933278 }", errors.get(2).getTmddValue());

        assertEquals("End Point", errors.get(3).getName());
        assertEquals("{ lat: 43.180000, lon: -111.010000 }", errors.get(3).getTimValue());
        assertEquals("{ lat: 43.175668, lon: -111.001784 }", errors.get(3).getTmddValue());

        assertEquals("ITIS Codes", errors.get(4).getName());
        assertEquals("{ 5906 }", errors.get(4).getTimValue());
        assertEquals("{ 6011 }", errors.get(4).getTmddValue());

        // Email was sent
        verify(mockEmailHelper).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateTmdd_run_activeTimServiceError() throws MailException, MessagingException {
        // Arrange
        when(mockActiveTimService.getActiveTimsWithItisCodes()).thenThrow(new RestClientException("timeout"));

        // Act
        uut.run();

        // Assert
        verify(mockUtility, times(2)).logWithDate(logMessageCaptor.capture(), eq(ValidateTmdd.class));
        assertEquals("Error fetching Active Tims:", logMessageCaptor.getValue());
        verify(mockEmailHelper, times(0)).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateTmdd_run_tmddServiceError() throws Exception {
        // Arrange
        when(mockTmddService.getTmddEvents()).thenThrow(new RestClientException("timeout"));

        // Act
        uut.run();

        // Assert
        verify(mockUtility, times(2)).logWithDate(logMessageCaptor.capture(), eq(ValidateTmdd.class));
        assertEquals("Error fetching FEUs from TMDD:", logMessageCaptor.getValue());
        verify(mockEmailHelper, times(0)).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateTmdd_run_itisCodeServiceError() throws MailException, MessagingException {
        // Arrange
        when(mockItisCodeService.selectAllTmddItisCodes()).thenThrow(new RestClientException("timeout"));

        // Act
        uut.run();

        // Assert
        verify(mockUtility, times(2)).logWithDate(logMessageCaptor.capture(), eq(ValidateTmdd.class));
        assertEquals("Unable to initialize TMDD ITIS Code cache:", logMessageCaptor.getValue());
        verify(mockEmailHelper, times(0)).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateTmdd_run_emailSendError() throws Exception {
        // Arrange
        // Load in fake data so we can accumulate validation errors
        Gson tmddDeserializer = new GsonFactory().getTmddDeserializer();

        ActiveTim[] activeTims = importJsonArray("/activeTims_5.json", ActiveTim[].class);
        FullEventUpdate[] feus = importJsonArray("/feus_2.json", FullEventUpdate[].class, tmddDeserializer);
        TmddItisCode[] itisCodes = importJsonArray("/tmdd_itis_codes.json", TmddItisCode[].class);

        when(mockTmddService.getTmddEvents()).thenReturn(Arrays.asList(feus));
        when(mockActiveTimService.getActiveTimsWithItisCodes()).thenReturn(Arrays.asList(activeTims));
        when(mockItisCodeService.selectAllTmddItisCodes()).thenReturn(Arrays.asList(itisCodes));

        // Throw exception when sending email
        doThrow(new MailSendException("unable to send")).when(mockEmailHelper).SendEmail(any(), any(), any(), any(),
                any(), any(), any());

        // Act
        uut.run();

        // Assert
        verify(mockUtility, times(2)).logWithDate(logMessageCaptor.capture(), eq(ValidateTmdd.class));
        assertEquals("Error sending summary email:", logMessageCaptor.getValue());
    }
}