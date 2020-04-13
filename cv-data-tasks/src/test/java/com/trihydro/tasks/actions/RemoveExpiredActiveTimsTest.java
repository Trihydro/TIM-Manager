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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RestTemplateProvider.class })
public class RemoveExpiredActiveTimsTest {

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
    public RemoveExpiredActiveTims uut;

    @Before
    public void setup() {
        PowerMockito.mockStatic(RestTemplateProvider.class);

        when(mockRestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);

        List<ActiveTim> expiredTims = new ArrayList<ActiveTim>();
        expiredTims.add(new ActiveTim());
        expiredTims.add(new ActiveTim());
        Mockito.when(mockActiveTimService.getExpiredActiveTims()).thenReturn(expiredTims);
    }

    @Test
    public void cleanupActiveTims_runTest() {
        uut.run();

        // assert exchange called twice
        verify(mockRestTemplate, Mockito.times(2)).exchange(any(String.class), any(HttpMethod.class),
                Mockito.<HttpEntity<String>>any(), Mockito.<Class<String>>any());
    }
}