package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.helpers.GsonFactory;
import com.trihydro.library.model.TmddProps;
import com.trihydro.library.model.tmdd.FullEventUpdate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

@RunWith(StrictStubs.class)
public class TmddServiceTest extends BaseServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    TmddProps mockConfig;

    @Mock
    ResponseEntity<String> mockResponse;

    TmddService uut;

    @Before
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
        assertEquals(0, result.size());
    }

    @Test
    public void getTmddEvents_badResponse() throws Exception {
        // Define our Assertions
        thrown.expect(Exception.class);
        thrown.expectMessage("Response from TMDD doesn't have the expected structure");

        // Arrange
        String response = "{}";
        when(mockResponse.getBody()).thenReturn(response);

        // Act
        uut.getTmddEvents();
    }

    @Test
    public void getTmddEvents_restClientException() throws Exception {
        // Define our Assertions
        thrown.expect(RestClientException.class);

        // Arrange
        when(mockResponse.getBody()).thenThrow(new RestClientException("unable to connect"));

        // Act
        uut.getTmddEvents();
    }

}