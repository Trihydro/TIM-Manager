package com.trihydro.tasks.actions;

import static com.trihydro.tasks.TestHelper.importJsonArray;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

import com.google.gson.Gson;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.GsonFactory;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ActiveTimError;
import com.trihydro.library.model.ActiveTimValidationResult;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.TimDeleteSummary;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.model.TmddItisCode;
import com.trihydro.library.model.tmdd.FullEventUpdate;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.TmddService;
import com.trihydro.library.service.WydotTimService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.helpers.EmailFormatter;
import com.trihydro.tasks.helpers.IdNormalizer;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private WydotTimService mockWydotTimService;

    @Mock
    private TimGenerationHelper mockTimGenerationHelper;

    @Captor
    private ArgumentCaptor<List<ActiveTim>> unableToVerifyCaptor;

    @Captor
    private ArgumentCaptor<List<ActiveTimValidationResult>> validationResultsCaptor;

    @Captor
    private ArgumentCaptor<String> logMessageCaptor;

    @Captor
    private ArgumentCaptor<String> exceptionMessageCaptor;

    @InjectMocks
    ValidateTmdd uut;

    @Test
    public void validateTmdd_run_noRecords() throws Exception {
        // Act
        uut.run();

        // Assert
        verify(mockTmddService).getTmddEvents();
        verify(mockActiveTimService).getActiveTimsWithItisCodes(true);

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
        when(mockActiveTimService.getActiveTimsWithItisCodes(true)).thenReturn(Arrays.asList(activeTims));
        when(mockItisCodeService.selectAllTmddItisCodes()).thenReturn(Arrays.asList(itisCodes));

        // Act
        uut.run();

        // Assert
        // Service methods called
        verify(mockTmddService).getTmddEvents();
        verify(mockActiveTimService).getActiveTimsWithItisCodes(true);
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
        when(mockActiveTimService.getActiveTimsWithItisCodes(true)).thenReturn(Arrays.asList(activeTims));
        when(mockItisCodeService.selectAllTmddItisCodes()).thenReturn(Arrays.asList(itisCodes));

        // Act
        uut.run();

        // Assert
        // Service methods called
        verify(mockTmddService).getTmddEvents();
        verify(mockActiveTimService).getActiveTimsWithItisCodes(true);
        verify(mockItisCodeService).selectAllTmddItisCodes();

        // Validation summary email generated
        verify(mockEmailFormatter).generateTmddSummaryEmail(unableToVerifyCaptor.capture(),
                validationResultsCaptor.capture(), exceptionMessageCaptor.capture());

        List<ActiveTim> unableToVerify = unableToVerifyCaptor.getValue();
        List<ActiveTimValidationResult> validationResults = validationResultsCaptor.getValue();

        // Check validation results
        Assertions.assertEquals(0, unableToVerify.size());
        Assertions.assertTrue(StringUtils.isBlank(exceptionMessageCaptor.getValue()));
        Assertions.assertEquals(1, validationResults.size());
        Assertions.assertEquals(1l, (long) validationResults.get(0).getActiveTim().getActiveTimId());
        Assertions.assertEquals(4, validationResults.get(0).getErrors().size());

        // Check errors individually
        List<ActiveTimError> errors = validationResults.get(0).getErrors();

        Assertions.assertEquals("End Time", errors.get(0).getName().getStringValue());
        Assertions.assertEquals("2020-04-27 11:00:00", errors.get(0).getTimValue());
        Assertions.assertEquals(null, errors.get(0).getTmddValue());

        var gson = new Gson();
        Assertions.assertEquals("Start Point", errors.get(1).getName().getStringValue());
        var c1 = new Coordinate(BigDecimal.valueOf(42.75), BigDecimal.valueOf(-110.94));
        var c2 = new Coordinate(BigDecimal.valueOf(42.739996), BigDecimal.valueOf(-110.933278));
        Assertions.assertEquals(gson.toJson(c1), errors.get(1).getTimValue());
        Assertions.assertEquals(gson.toJson(c2), errors.get(1).getTmddValue());

        var c3 = new Coordinate(BigDecimal.valueOf(43.18), BigDecimal.valueOf(-111.01));
        var c4 = new Coordinate(BigDecimal.valueOf(43.175668), BigDecimal.valueOf(-111.001784));
        Assertions.assertEquals("End Point", errors.get(2).getName().getStringValue());
        Assertions.assertEquals(gson.toJson(c3), errors.get(2).getTimValue());
        Assertions.assertEquals(gson.toJson(c4), errors.get(2).getTmddValue());

        Assertions.assertEquals("ITIS Codes", errors.get(3).getName().getStringValue());
        Assertions.assertEquals("{ 5906 }", errors.get(3).getTimValue());
        Assertions.assertEquals("{ 6011 }", errors.get(3).getTmddValue());

        // Email was sent
        verify(mockEmailHelper).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateTmdd_run_unableToVerifyErrors() throws Exception {
        // Arrange
        Gson tmddDeserializer = new GsonFactory().getTmddDeserializer();

        ActiveTim[] activeTims = importJsonArray("/activeTims_5.json", ActiveTim[].class);
        FullEventUpdate[] feus = importJsonArray("/feus_3.json", FullEventUpdate[].class, tmddDeserializer);
        TmddItisCode[] itisCodes = importJsonArray("/tmdd_itis_codes.json", TmddItisCode[].class);

        when(mockTmddService.getTmddEvents()).thenReturn(Arrays.asList(feus));
        when(mockActiveTimService.getActiveTimsWithItisCodes(true)).thenReturn(Arrays.asList(activeTims));
        when(mockItisCodeService.selectAllTmddItisCodes()).thenReturn(Arrays.asList(itisCodes));

        TimDeleteSummary tds = new TimDeleteSummary();
        tds.setSatelliteErrorSummary("satellite error summary");
        tds.addFailedActiveTimDeletions(-1l);
        TimRsu timRsu = new TimRsu();
        timRsu.setRsuId(-1l);
        timRsu.setRsuIndex(22);
        timRsu.setTimId(-2l);
        var gson = new Gson();
        tds.addfailedRsuTimJson(gson.toJson(timRsu));
        when(mockWydotTimService.deleteTimsFromRsusAndSdx(any())).thenReturn(tds);

        // Act
        uut.run();

        // Assert
        // Service methods called
        verify(mockTmddService).getTmddEvents();
        verify(mockActiveTimService).getActiveTimsWithItisCodes(true);
        verify(mockItisCodeService).selectAllTmddItisCodes();

        // Validation summary email generated
        verify(mockEmailFormatter).generateTmddSummaryEmail(unableToVerifyCaptor.capture(),
                validationResultsCaptor.capture(), exceptionMessageCaptor.capture());

        List<ActiveTim> unableToVerify = unableToVerifyCaptor.getValue();
        List<ActiveTimValidationResult> validationResults = validationResultsCaptor.getValue();
        String exMessage = exceptionMessageCaptor.getValue();

        // Check validation results
        Assertions.assertEquals(1, unableToVerify.size());
        Assertions.assertEquals(0, validationResults.size());

        String errSummary = tds.getSatelliteErrorSummary();
        errSummary += "<br/>";
        errSummary += "The following active tim record failed to delete: <br/>";
        errSummary += -1l;
        errSummary += "<br/>";
        errSummary += "<br/><br/>";
        errSummary += "The following RsuTim records failed to remove values from associated RSUs: <br/>";
        errSummary += tds.getRsuErrorSummary();
        Assertions.assertEquals(errSummary, exMessage);

        // Email was sent
        verify(mockEmailHelper).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateTmdd_run_activeTimServiceError() throws MailException, MessagingException {
        // Arrange
        when(mockActiveTimService.getActiveTimsWithItisCodes(true)).thenThrow(new RestClientException("timeout"));

        // Act
        uut.run();

        // Assert
        verify(mockUtility, times(2)).logWithDate(logMessageCaptor.capture(), eq(ValidateTmdd.class));
        Assertions.assertEquals("Error fetching Active Tims:", logMessageCaptor.getValue());
        verify(mockEmailHelper).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateTmdd_run_tmddServiceError() throws Exception {
        // Arrange
        when(mockTmddService.getTmddEvents()).thenThrow(new RestClientException("timeout"));

        // Act
        uut.run();

        // Assert
        verify(mockUtility, times(2)).logWithDate(logMessageCaptor.capture(), eq(ValidateTmdd.class));
        Assertions.assertEquals("Error fetching FEUs from TMDD:", logMessageCaptor.getValue());
        verify(mockEmailHelper).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateTmdd_run_itisCodeServiceError() throws MailException, MessagingException {
        // Arrange
        when(mockItisCodeService.selectAllTmddItisCodes()).thenThrow(new RestClientException("timeout"));

        // Act
        uut.run();

        // Assert
        verify(mockUtility, times(2)).logWithDate(logMessageCaptor.capture(), eq(ValidateTmdd.class));
        Assertions.assertEquals("Unable to initialize TMDD ITIS Code cache:", logMessageCaptor.getValue());
        verify(mockEmailHelper).SendEmail(any(), any(), any(), any(), any(), any(), any());
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
        when(mockActiveTimService.getActiveTimsWithItisCodes(true)).thenReturn(Arrays.asList(activeTims));
        when(mockItisCodeService.selectAllTmddItisCodes()).thenReturn(Arrays.asList(itisCodes));

        // Throw exception when sending email
        doThrow(new MailSendException("unable to send")).when(mockEmailHelper).SendEmail(any(), any(), any(), any(),
                any(), any(), any());

        // Act
        uut.run();

        // Assert
        verify(mockUtility, times(3)).logWithDate(logMessageCaptor.capture(), eq(ValidateTmdd.class));
        Assertions.assertEquals("Error sending summary email:", logMessageCaptor.getAllValues().get(1));
        Assertions.assertEquals("Failed to send error summary email:", logMessageCaptor.getAllValues().get(2));
    }
}