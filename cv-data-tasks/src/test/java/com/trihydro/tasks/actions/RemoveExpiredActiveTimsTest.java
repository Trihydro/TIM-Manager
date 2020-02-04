package com.trihydro.tasks.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
public class RemoveExpiredActiveTimsTest {

    @Mock
    private DataTasksConfiguration mockConfig;

    @Mock
    private RestTemplate mockRestTemplate;

    @InjectMocks
    public RemoveExpiredActiveTims uut;

    @Before
    public void setup() {
        PowerMockito.mockStatic(ActiveTimService.class);
        PowerMockito.mockStatic(RestTemplateProvider.class);

        when(RestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);

        List<ActiveTim> expiredTims = new ArrayList<ActiveTim>();
        expiredTims.add(new ActiveTim());
        expiredTims.add(new ActiveTim());
        Mockito.when(ActiveTimService.getExpiredActiveTims()).thenReturn(expiredTims);
    }

    @Test
    public void cleanupActiveTims_runTest() {
        uut.run();

        // assert exchange called twice
        verify(mockRestTemplate, Mockito.times(2)).exchange(any(String.class), any(HttpMethod.class),
                Matchers.<HttpEntity<String>>any(), Matchers.<Class<String>>any());
    }
}