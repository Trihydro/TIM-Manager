package com.trihydro.tasks.actions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.tasks.config.DataTasksConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class CleanupActiveTimsTest {

    @Mock
    private DataTasksConfiguration mockConfig;
    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    Utility mockUtility;
    @Mock
    ActiveTimService mockActiveTimService;
    @Mock
    RestTemplateProvider mockRestTemplateProvider;

    @InjectMocks
    public CleanupActiveTims uut;

    @BeforeEach
    public void setup() {
        when(mockRestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);

        List<ActiveTim> itisTims = new ArrayList<ActiveTim>();
        itisTims.add(new ActiveTim());
        when(mockActiveTimService.getActiveTimsMissingItisCodes()).thenReturn(itisTims);

        List<ActiveTim> notSentTims = new ArrayList<ActiveTim>();
        notSentTims.add(new ActiveTim());
        when(mockActiveTimService.getActiveTimsNotSent()).thenReturn(notSentTims);
    }

    @Test
    public void cleanupActiveTims_runTest() {
        uut.run();

        // assert exchange called twice
        verify(mockRestTemplate, Mockito.times(2)).exchange(any(String.class), any(HttpMethod.class),
                Mockito.<HttpEntity<String>>any(), Mockito.<Class<String>>any());
    }
}