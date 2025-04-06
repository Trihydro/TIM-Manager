package com.trihydro.timrefresh;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.mail.MessagingException;

import com.google.gson.Gson;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.Logging_TimUpdateModel;
import com.trihydro.library.model.ResubmitTimException;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.SdwService;
import com.trihydro.timrefresh.config.TimRefreshConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

@ExtendWith(MockitoExtension.class)
public class TimRefreshControllerTest {
    private static final Logger LOG = LoggerFactory.getLogger(TimRefreshControllerTest.class);
    private long timID = 1l;

    @Mock
    TimRefreshConfiguration mockConfiguration;
    @Mock
    SdwService mockSdwService;
    @Mock
    Utility mockUtility;
    @Mock
    ActiveTimService mockActiveTimService;
    @Mock
    EmailHelper mockEmailHelper;
    @Mock
    TimGenerationHelper mockTimGenerationHelper;

    @InjectMocks
    private TimRefreshController controllerUnderTest;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        LOG.info("Executing {}", testInfo.getTestMethod().get().getName());
    }

    @Test
    public void TestPerformTaskUsingCron_NoData() {
        // setup return
        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(new ArrayList<TimUpdateModel>());
        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify getExpiringActiveTims called once
        verify(mockActiveTimService).getExpiringActiveTims();
        // verify no further interactions on ActiveTimService
        verifyNoMoreInteractions(mockActiveTimService);
        // verify no emails sent
        verifyNoInteractions(mockEmailHelper);
    }

    @Test
    public void TestPerformTaskUsingCron_InvalidData() throws MailException, MessagingException {
        // Arrange
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
        TimUpdateModel tum = getRsuTim();
        tum.setStartPoint(new Coordinate());
        arrLst.add(tum);

        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
        when(mockTimGenerationHelper.isValidTim(any())).thenReturn(false);

        // Act
        controllerUnderTest.performTaskUsingCron();

        // Assert
        Gson gson = new Gson();
        String body = "The Tim Refresh application found invalid TIM(s) while attempting to refresh.";
        body += "<br/>";
        body += "The associated ActiveTim records are: <br/>";
        body += gson.toJson(new Logging_TimUpdateModel(tum));
        body += "<br/><br/>";
        verify(mockTimGenerationHelper).isValidTim(any());
        verify(mockEmailHelper).SendEmail(mockConfiguration.getAlertAddresses(), "TIM Refresh Exceptions", body);
        verifyNoMoreInteractions(mockTimGenerationHelper);
    }

    @Test
    public void TestPerformTaskUsingCron_RefreshException() throws MailException, MessagingException {
        // setup return
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
        TimUpdateModel tum = getRsuTim();
        arrLst.add(tum);

        List<ResubmitTimException> resubExceptions = new ArrayList<>();
        resubExceptions.add(new ResubmitTimException(-1l, "exception message"));

        when(mockActiveTimService.resetActiveTimsExpirationDate(any())).thenReturn(false);
        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
        when(mockTimGenerationHelper.isValidTim(any())).thenReturn(true);
        when(mockTimGenerationHelper.resetTimStartTimeAndResubmitToOde(any())).thenReturn(resubExceptions);

        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify static functions were called
        verify(mockTimGenerationHelper).resetTimStartTimeAndResubmitToOde(Collections.singletonList(tum.getActiveTimId()));
        var gson = new Gson();
        String body = "An error occurred while resetting the expiration date(s) for the Active TIM(s)<br/><br/>";
        body += "The TIM Refresh application ran into exceptions while attempting to resubmit TIMs. The following exceptions were found: ";
        body += "<br/>";
        body += gson.toJson(resubExceptions.get(0));
        body += "<br/>";
        verify(mockEmailHelper).SendEmail(mockConfiguration.getAlertAddresses(), "TIM Refresh Exceptions", body);
    }

    @Test
    public void TestPerformTaskUsingCron_Success() throws MailException, MessagingException {
        // setup return
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
        TimUpdateModel tum = getRsuTim();
        arrLst.add(tum);

        List<ResubmitTimException> resubExceptions = new ArrayList<>();

        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
        when(mockActiveTimService.resetActiveTimsExpirationDate(any())).thenReturn(true);
        when(mockTimGenerationHelper.isValidTim(any())).thenReturn(true);
        when(mockTimGenerationHelper.resetTimStartTimeAndResubmitToOde(any())).thenReturn(resubExceptions);

        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify static functions were called
        verify(mockTimGenerationHelper).resetTimStartTimeAndResubmitToOde(Collections.singletonList(tum.getActiveTimId()));
        // verify no emails sent
        verifyNoInteractions(mockEmailHelper);
    }

    private TimUpdateModel getTumBase() {
        TimUpdateModel tum = new TimUpdateModel();
        tum.setRoute("I 80");
        tum.setDirection("i");
        tum.setStartPoint(new Coordinate(BigDecimal.valueOf(-1), BigDecimal.valueOf(-2)));
        tum.setEndPoint(new Coordinate(BigDecimal.valueOf(-3), BigDecimal.valueOf(-4)));
        tum.setClosedPath(false);
        return tum;
    }

    private TimUpdateModel getRsuTim() {
        TimUpdateModel tum = getTumBase();
        tum.setRoute("I 80");
        tum.setRsuTarget("DefaultTarget");
        tum.setClosedPath(false);
        tum.setTimId(timID);

        tum.setStartDate_Timestamp(new Timestamp(System.currentTimeMillis()));
        tum.setEndDate_Timestamp(new Timestamp(System.currentTimeMillis()));
        return tum;
    }
}