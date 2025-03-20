package com.trihydro.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.model.CVRestServiceProps;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class ActiveTimHoldingServiceTest extends BaseServiceTest {

    @Mock
    private CVRestServiceProps mockConfig;

    @InjectMocks
    private ActiveTimHoldingService uut;

    private final String baseUrl = "baseUrl";

    @BeforeEach
    public void setupSubTest() throws SQLException {
        doReturn(baseUrl).when(mockConfig).getCvRestService();
    }

    @Test
    void getAllRecords_SuccessfulRetrieval_ShouldReturnRecords() {
        // Arrange
        ActiveTimHolding[] mockRecordsArray = new ActiveTimHolding[1];
        ActiveTimHolding mockRecord = new ActiveTimHolding();
        mockRecordsArray[0] = mockRecord;
        ResponseEntity<ActiveTimHolding[]> mockResponse = ResponseEntity.ok(mockRecordsArray);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        when(mockRestTemplate.exchange(baseUrl + "/active-tim-holding/get-all", HttpMethod.GET, requestEntity, ActiveTimHolding[].class)).thenReturn(mockResponse);

        // Act
        List<ActiveTimHolding> result = uut.getAllRecords();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void deleteActiveTimHolding_SuccessfulDelete_ShouldReturnTrue() {
        // Arrange
        Long mockId = 1L;
        ResponseEntity<Boolean> mockResponse = ResponseEntity.ok(true);
        when(mockRestTemplate.exchange(baseUrl + "/active-tim-holding/delete/" + mockId, HttpMethod.DELETE, null, Boolean.class)).thenReturn(mockResponse);

        // Act
        boolean result = uut.deleteActiveTimHolding(mockId);

        // Assert
        assertTrue(result);
    }

    @Test
    void deleteActiveTimHolding_FailedDelete_ShouldReturnFalse() {
        // Arrange
        Long mockId = 1L;
        ResponseEntity<Boolean> mockResponse = ResponseEntity.ok(false);
        when(mockRestTemplate.exchange(baseUrl + "/active-tim-holding/delete/" + mockId, HttpMethod.DELETE, null, Boolean.class)).thenReturn(mockResponse);

        // Act
        boolean result = uut.deleteActiveTimHolding(mockId);

        // Assert
        assertFalse(result);
    }

    @Test
    void deleteActiveTimHolding_WhenDatabaseConnectionFails_ShouldThrowException() {
        // Arrange
        Long mockId = 1L;
        when(mockRestTemplate.exchange(baseUrl + "/active-tim-holding/delete/" + mockId, HttpMethod.DELETE, null, Boolean.class)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> uut.deleteActiveTimHolding(mockId));
    }
}