package com.trihydro.tasks.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ActiveTimService.class, RestTemplateProvider.class })
public class CleanupActiveTimsTest {

    @Mock
    private DataTasksConfiguration mockConfig;

    @Mock
    private RestTemplate mockRestTemplate;
    
    @Mock 
    Utility mockUtility;

    @InjectMocks
    public CleanupActiveTims uut;

    @Before
    public void setup() {
        PowerMockito.mockStatic(ActiveTimService.class);
        PowerMockito.mockStatic(RestTemplateProvider.class);

        when(RestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);

        List<ActiveTim> itisTims = new ArrayList<ActiveTim>();
        itisTims.add(new ActiveTim());
        Mockito.when(ActiveTimService.getActiveTimsMissingItisCodes()).thenReturn(itisTims);

        List<ActiveTim> notSentTims = new ArrayList<ActiveTim>();
        notSentTims.add(new ActiveTim());
        Mockito.when(ActiveTimService.getActiveTimsNotSent()).thenReturn(notSentTims);
    }

    @Test
    public void cleanupActiveTims_runTest() {
        uut.run();

        // assert exchange called twice
        verify(mockRestTemplate, Mockito.times(2)).exchange(any(String.class), any(HttpMethod.class),
                Matchers.<HttpEntity<String>>any(), Matchers.<Class<String>>any());
    }
}