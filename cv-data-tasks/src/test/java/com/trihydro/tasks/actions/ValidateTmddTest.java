package com.trihydro.tasks.actions;

import static com.trihydro.tasks.TestHelper.importJsonArray;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;

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
}