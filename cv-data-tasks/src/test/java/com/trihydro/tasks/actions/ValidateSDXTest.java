package com.trihydro.tasks.actions;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import com.google.gson.Gson;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.SdwService;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mail.MailException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ValidateSDX.class })
public class ValidateSDXTest {
    @Mock
    private EmailHelper mockEmailHelper;

    @Mock
    private SdwService mockSdwService;

    @Mock
    private ActiveTimService mockActiveTimService;

    @InjectMocks
    ValidateSDX uut;

    Gson gson = new Gson();

    @Before
    public void setup() {
        // We shouldn't be calling any statics, but as a safeguard...
        PowerMockito.mockStatic(ActiveTimService.class);
    }

    @Test
    public void validateSDX_run_noRecords() throws MailException, MessagingException {
        uut.run();

        // Services were called
        verify(mockSdwService).getMsgsForOdeUser();
        verify(mockActiveTimService).getActiveTimsForSDX();

        // No email was sent
        verify(mockEmailHelper, times(0)).SendEmail(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any());
    }

    @Test
    public void validateSDX_run_success() throws MailException, MessagingException {
        ActiveTim[] activeTims = importJsonArray("/activeTims_1.json", ActiveTim[].class);
        AdvisorySituationDataDeposit[] asdds = importJsonArray("/asdds_1.json", AdvisorySituationDataDeposit[].class);

        // Arrange service responses
        when(mockSdwService.getMsgsForOdeUser()).thenReturn(Arrays.asList(asdds));
        when(mockActiveTimService.getActiveTimsForSDX()).thenReturn(Arrays.asList(activeTims));
        // Return ITIS codes for ASDDs
        doReturn(Arrays.asList(8,7,6)).when(mockSdwService).getItisCodesFromAdvisoryMessage("0");
        doReturn(Arrays.asList(17,16,18)).when(mockSdwService).getItisCodesFromAdvisoryMessage("-1");

        // Act
        uut.run();

        // Services were called
        verify(mockSdwService).getMsgsForOdeUser();
        verify(mockActiveTimService).getActiveTimsForSDX();

        // No email was sent
        verify(mockEmailHelper, times(0)).SendEmail(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any());

    }

    // TODO: ArgumentCaptor: https://stackoverflow.com/questions/36253040/example-of-mockitos-argumentcaptor

    <T> T importJsonArray(String fileName, Class<T> clazz) {
        InputStream is = ValidateSDXTest.class.getResourceAsStream(fileName);
        InputStreamReader isr = new InputStreamReader(is);

        T data = gson.fromJson(isr, clazz);

        try {
            isr.close();
        } catch (IOException ex) {

        }

        return data;
    }
}