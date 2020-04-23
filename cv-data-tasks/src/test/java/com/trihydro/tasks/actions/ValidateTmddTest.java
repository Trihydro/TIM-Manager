package com.trihydro.tasks.actions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.TmddService;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.helpers.EmailFormatter;
import com.trihydro.tasks.helpers.IdNormalizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;

@RunWith(StrictStubs.class)
public class ValidateTmddTest {
    @Mock
    private TmddService mockTmddService;

    @Mock
    private ActiveTimService mockActiveTimService;

    @Mock
    private Utility mockUtility;

    @Mock
    private IdNormalizer mockIdNormalizer;

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
}