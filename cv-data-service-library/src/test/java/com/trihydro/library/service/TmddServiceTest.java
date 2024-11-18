package com.trihydro.library.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.helpers.GsonFactory;
import com.trihydro.library.model.TmddProps;
import com.trihydro.library.model.tmdd.FullEventUpdate;

import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

public class TmddServiceTest extends BaseServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    TmddProps mockConfig;

    @Mock
    ResponseEntity<String> mockResponse;

    TmddService uut;

    @BeforeEach
    public void setupSubtest() {
        when(mockConfig.getTmddUrl()).thenReturn("endpoint");
        when(mockConfig.getTmddUser()).thenReturn("user");
        when(mockConfig.getTmddPassword()).thenReturn("password");

        // Using live GsonFactory to test "root node removal" behavior
        uut = new TmddService();
        uut.InjectDependencies(mockConfig, new GsonFactory(), mockRestTemplateProvider);

        // Configure standard mock behavior
        when(mockRestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);
        doReturn(mockResponse).when(mockRestTemplate).exchange(eq("endpoint/tmdd/all"), eq(HttpMethod.GET), any(),
                eq(String.class));
    }

    @Test
    public void getTmddEvents_success() throws Exception {
        // Arrange
        String response = "{\"ns2:fEUMsg\": {\"FEU\": []}}";
        when(mockResponse.getBody()).thenReturn(response);

        // Act
        List<FullEventUpdate> result = uut.getTmddEvents();

        // Assert
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void getTmddEvents_badResponse() throws Exception {
        // Define our Assertions
        
        // Arrange
        String response = "{}";
        when(mockResponse.getBody()).thenReturn(response);

        // Act
        var ex = Assertions.assertThrows(Exception.class, () -> uut.getTmddEvents());
        Assertions.assertEquals("Response from TMDD doesn't have the expected structure", ex.getMessage());

    }

    @Test
    public void getTmddEvents_restClientException() throws Exception {
        // Define our Assertions
        thrown.expect(RestClientException.class);

        // Arrange
        when(mockResponse.getBody()).thenThrow(new RestClientException("unable to connect"));

        // Act
        Assertions.assertThrows(RestClientException.class,()-> uut.getTmddEvents());
    }

}