package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.IncidentChoice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class IncidentChoicesServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<IncidentChoice[]> mockResponseEntityIncidentChoiceArray;

    private IncidentChoice ic;

    private String baseUrl = "baseUrl";

    @Mock
    private CVRestServiceProps mockConfig;

    @InjectMocks
    private IncidentChoicesService uut;

    @BeforeEach
    public void setupSubTest() {
        IncidentChoice[] icArr = new IncidentChoice[1];
        ic = new IncidentChoice();
        ic.setCode("code");
        ic.setDescription("description");
        icArr[0] = ic;
        doReturn(icArr).when(mockResponseEntityIncidentChoiceArray).getBody();

        doReturn(baseUrl).when(mockConfig).getCvRestService();
    }

    @Test
    public void selectAllIncidentActions() {
        // Arrange
        String url = String.format("%s/incident-choice/incident-actions", baseUrl);
        HttpEntity<String> entity = getEntity(null, String.class);
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, IncidentChoice[].class))
                .thenReturn(mockResponseEntityIncidentChoiceArray);

        // Act
        List<IncidentChoice> data = uut.selectAllIncidentActions();

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, IncidentChoice[].class);
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals(ic, data.get(0));
    }

    @Test
    public void selectAllIncidentEffects() {
        // Arrange
        String url = String.format("%s/incident-choice/incident-effects", baseUrl);
        HttpEntity<String> entity = getEntity(null, String.class);
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, IncidentChoice[].class))
                .thenReturn(mockResponseEntityIncidentChoiceArray);

        // Act
        List<IncidentChoice> data = uut.selectAllIncidentEffects();

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, IncidentChoice[].class);
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals(ic, data.get(0));
    }

    @Test
    public void selectAllIncidentProblems() {
        // Arrange
        String url = String.format("%s/incident-choice/incident-problems", baseUrl);
        HttpEntity<String> entity = getEntity(null, String.class);
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, IncidentChoice[].class))
                .thenReturn(mockResponseEntityIncidentChoiceArray);

        // Act
        List<IncidentChoice> data = uut.selectAllIncidentProblems();

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, IncidentChoice[].class);
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals(ic, data.get(0));
    }
}