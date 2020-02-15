package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.IncidentChoice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)

public class IncidentChoicesServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<IncidentChoice[]> mockResponseEntityIncidentChoiceArray;

    private IncidentChoice ic;

    @Before
    public void setupSubTest() {
        IncidentChoice[] icArr = new IncidentChoice[1];
        ic = new IncidentChoice();
        ic.setCode("code");
        ic.setDescription("description");
        icArr[0] = ic;
        doReturn(icArr).when(mockResponseEntityIncidentChoiceArray).getBody();
    }

    @Test
    public void selectAllIncidentActions() {
        // Arrange
        String url = "null/incident-choice/incident-actions";
        HttpEntity<String> entity = getEntity(null, String.class);
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, IncidentChoice[].class))
                .thenReturn(mockResponseEntityIncidentChoiceArray);

        // Act
        List<IncidentChoice> data = IncidentChoicesService.selectAllIncidentActions();

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, IncidentChoice[].class);
        assertEquals(1, data.size());
        assertEquals(ic, data.get(0));
    }

    @Test
    public void selectAllIncidentEffects() {
        // Arrange
        String url = "null/incident-choice/incident-effects";
        HttpEntity<String> entity = getEntity(null, String.class);
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, IncidentChoice[].class))
                .thenReturn(mockResponseEntityIncidentChoiceArray);

        // Act
        List<IncidentChoice> data = IncidentChoicesService.selectAllIncidentEffects();

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, IncidentChoice[].class);
        assertEquals(1, data.size());
        assertEquals(ic, data.get(0));
    }

    @Test
    public void selectAllIncidentProblems(){
       // Arrange
       String url = "null/incident-choice/incident-problems";
       HttpEntity<String> entity = getEntity(null, String.class);
       when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, IncidentChoice[].class))
               .thenReturn(mockResponseEntityIncidentChoiceArray);

       // Act
       List<IncidentChoice> data = IncidentChoicesService.selectAllIncidentProblems();

       // Assert
       verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, IncidentChoice[].class);
       assertEquals(1, data.size());
       assertEquals(ic, data.get(0));
    }
}