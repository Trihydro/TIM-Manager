package com.trihydro.tasks.actions;

import static com.trihydro.tasks.TestHelper.importJsonArray;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.SemiDialogID;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.SdwService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.helpers.EmailFormatter;
import com.trihydro.tasks.models.CActiveTim;
import com.trihydro.tasks.models.CAdvisorySituationDataDeposit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailException;

public class ValidateSDXTest {
    // Mocked dependencies
    @Mock
    private EmailHelper mockEmailHelper;
    @Mock
    private SdwService mockSdwService;
    @Mock
    private ActiveTimService mockActiveTimService;
    @Mock
    private EmailFormatter mockEmailFormatter;
    @Mock
    private DataTasksConfiguration mockConfig;
    @Mock
    private Utility mockUtility;

    // Argument Captors
    @Captor
    private ArgumentCaptor<List<CActiveTim>> toResendCaptor;
    @Captor
    private ArgumentCaptor<List<CAdvisorySituationDataDeposit>> deleteFromSdxCaptor;
    @Captor
    private ArgumentCaptor<List<CActiveTim>> invOracleRecordsCaptor;

    // Unit under test
    @InjectMocks
    ValidateSDX uut;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateSDX_run_noRecords() throws MailException, MessagingException {
        uut.run();

        // Services were called
        verify(mockSdwService).getMsgsForOdeUser(SemiDialogID.AdvSitDataDep);
        verify(mockActiveTimService).getActiveTimsForSDX();

        // No email was sent
        verify(mockEmailHelper, times(0)).SendEmail(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void validateSDX_run_allValid() throws MailException, MessagingException {
        ActiveTim[] activeTims = importJsonArray("/activeTims_1.json", ActiveTim[].class);
        AdvisorySituationDataDeposit[] asdds = importJsonArray("/asdds_1.json", AdvisorySituationDataDeposit[].class);

        // Arrange service responses
        when(mockSdwService.getMsgsForOdeUser(SemiDialogID.AdvSitDataDep)).thenReturn(Arrays.asList(asdds));
        when(mockActiveTimService.getActiveTimsForSDX()).thenReturn(Arrays.asList(activeTims));
        // Return ITIS codes for ASDDs
        doReturn(Arrays.asList(8, 7, 6)).when(mockSdwService).getItisCodesFromAdvisoryMessage("0");
        doReturn(Arrays.asList(17, 16, 18)).when(mockSdwService).getItisCodesFromAdvisoryMessage("-1");

        // Act
        uut.run();

        // Assert
        // Services were called
        verify(mockSdwService).getMsgsForOdeUser(SemiDialogID.AdvSitDataDep);
        verify(mockActiveTimService).getActiveTimsForSDX();
        verify(mockSdwService, times(2)).getItisCodesFromAdvisoryMessage(any());

        // No email was sent
        verify(mockEmailHelper, times(0)).SendEmail(any(), any(), any(), any(), any(), any(), any());

    }

    @Test
    public void validateSDX_noSdx() throws MailException, MessagingException {
        // Arrange
        // 2 Active TIMs, 0 SDX.
        ActiveTim[] activeTims = importJsonArray("/activeTims_1.json", ActiveTim[].class);
        AdvisorySituationDataDeposit[] asdds = new AdvisorySituationDataDeposit[0];

        // Arrange service responses
        when(mockSdwService.getMsgsForOdeUser(SemiDialogID.AdvSitDataDep)).thenReturn(Arrays.asList(asdds));
        when(mockActiveTimService.getActiveTimsForSDX()).thenReturn(Arrays.asList(activeTims));

        // Act
        uut.run();

        // Assert
        // 2 Active TIMs, with 0 records in the SDX. We're expecting:
        // - Number of Oracle records without corresponding message in SDX: 2
        // - toResend to contain 2 records

        // Services were called
        verify(mockSdwService).getMsgsForOdeUser(SemiDialogID.AdvSitDataDep);
        verify(mockActiveTimService).getActiveTimsForSDX();

        // Email was sent
        verify(mockEmailHelper).SendEmail(any(), any(), any(), any(), any(), any(), any());

        // Email had expected counts
        verify(mockEmailFormatter).generateSdxSummaryEmail(eq(0), eq(0), eq(2), toResendCaptor.capture(),
                deleteFromSdxCaptor.capture(), invOracleRecordsCaptor.capture());

        assertEquals(2, toResendCaptor.getValue().size());
        assertEquals(0, deleteFromSdxCaptor.getValue().size());
        assertEquals(0, invOracleRecordsCaptor.getValue().size());
    }

    @Test
    public void validateSDX_noOracle() throws MailException, MessagingException {
        // Arrange
        // 0 Active TIMS, 2 SDX.
        ActiveTim[] activeTims = new ActiveTim[0];
        AdvisorySituationDataDeposit[] asdds = importJsonArray("/asdds_1.json", AdvisorySituationDataDeposit[].class);

        // Arrange service responses
        when(mockSdwService.getMsgsForOdeUser(SemiDialogID.AdvSitDataDep)).thenReturn(Arrays.asList(asdds));
        when(mockActiveTimService.getActiveTimsForSDX()).thenReturn(Arrays.asList(activeTims));

        // Act
        uut.run();

        // Assert
        // 0 Active TIMs, with 2 records in the SDX. We're expecting:
        // - Number of messages on SDX without corresponding Oracle record: 2
        // - deleteFromSdx to contain 2 records

        // Services were called
        verify(mockSdwService).getMsgsForOdeUser(SemiDialogID.AdvSitDataDep);
        verify(mockActiveTimService).getActiveTimsForSDX();

        // Email was sent
        verify(mockEmailHelper).SendEmail(any(), any(), any(), any(), any(), any(), any());

        // Email had expected counts
        verify(mockEmailFormatter).generateSdxSummaryEmail(eq(2), eq(0), eq(0), toResendCaptor.capture(),
                deleteFromSdxCaptor.capture(), invOracleRecordsCaptor.capture());

        assertEquals(0, toResendCaptor.getValue().size());
        assertEquals(2, deleteFromSdxCaptor.getValue().size());
        assertEquals(0, invOracleRecordsCaptor.getValue().size());
    }

    @Test
    public void validateSDX_mixSuccess() throws MailException, MessagingException {
        // 3 Active TIMs, 3 SDX records.
        // 2 Active TIM and SDX records are aligned. Of those, 1 pair is accurate while
        // another is "stale".
        // The last Active TIM isn't present on the SDX, and the last SDX record is
        // orphaned.
        ActiveTim[] activeTims = importJsonArray("/activeTims_2.json", ActiveTim[].class);
        AdvisorySituationDataDeposit[] asdds = importJsonArray("/asdds_2.json", AdvisorySituationDataDeposit[].class);

        // Arrange service responses
        when(mockSdwService.getMsgsForOdeUser(SemiDialogID.AdvSitDataDep)).thenReturn(Arrays.asList(asdds));
        when(mockActiveTimService.getActiveTimsForSDX()).thenReturn(Arrays.asList(activeTims));
        // Return ITIS codes for ASDDs
        doReturn(Arrays.asList(8, 7, 6)).when(mockSdwService).getItisCodesFromAdvisoryMessage("0");
        // Stale record
        doReturn(Arrays.asList(0)).when(mockSdwService).getItisCodesFromAdvisoryMessage("-1");

        // Act
        uut.run();

        // Assert
        // We're expecting:
        // - Number of stale records on SDX (different ITIS codes than ActiveTim): 1
        // - Number of messages on SDX without corresponding Oracle record: 1
        // - Number of Oracle records without corresponding message in SDX: 1
        // - toResend count: 1
        // - deleteFromSdx count: 1
        // - invOracleRecords count: 0

        // Services were called
        verify(mockSdwService).getMsgsForOdeUser(SemiDialogID.AdvSitDataDep);
        verify(mockActiveTimService).getActiveTimsForSDX();
        verify(mockSdwService, times(3)).getItisCodesFromAdvisoryMessage(any());

        // Email was sent
        verify(mockEmailHelper).SendEmail(any(), any(), any(), any(), any(), any(), any());

        // Email had expected counts
        verify(mockEmailFormatter).generateSdxSummaryEmail(eq(1), eq(1), eq(1), toResendCaptor.capture(),
                deleteFromSdxCaptor.capture(), invOracleRecordsCaptor.capture());

        assertEquals(2, toResendCaptor.getValue().size());
        assertEquals(1, deleteFromSdxCaptor.getValue().size());
        assertEquals(0, invOracleRecordsCaptor.getValue().size());
    }
}