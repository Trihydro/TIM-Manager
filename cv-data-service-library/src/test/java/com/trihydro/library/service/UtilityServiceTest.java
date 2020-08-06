package com.trihydro.library.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.BsmCoreDataPartition;
import com.trihydro.library.model.CVRestServiceProps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

public class UtilityServiceTest extends BaseServiceTest {
    @Mock
    CVRestServiceProps mockConfig;

    @Mock
    ResponseEntity<List<String>> mockListStringResponse;
    @Mock
    ResponseEntity<Boolean> mockBoolResponse;

    @InjectMocks
    private UtilityService uut;

    @BeforeEach
    public void setupSubTest() {
        doReturn("base").when(mockConfig).getCvRestService();
    }

    @Test
    public void getBsmCoreDataPartitions_success() {
        // Arrange
        doReturn(mockListStringResponse).when(mockRestTemplate).getForEntity("base/utility/bsm-core-data-partitions",
                BsmCoreDataPartition[].class);
        doReturn(new BsmCoreDataPartition[0]).when(mockListStringResponse).getBody();

        // Act
        var response = uut.getBsmCoreDataPartitions();

        // Assert
        assertNotNull(response);
        verify(mockRestTemplate).getForEntity("base/utility/bsm-core-data-partitions", BsmCoreDataPartition[].class);
    }

    @Test
    public void getBsmCoreDataPartitions_restClientException() {
        // Arrange
        doThrow(new RestClientException("something went wrong...")).when(mockRestTemplate)
                .getForEntity("base/utility/bsm-core-data-partitions", BsmCoreDataPartition[].class);

        // Act, Assert
        assertThrows(RestClientException.class, () -> {
            uut.getBsmCoreDataPartitions();
        });
    }

    @Test
    public void dropBsmPartitions_success() {
        // Arrange
        when(mockRestTemplate.exchange(isA(String.class), eq(HttpMethod.POST), any(), eq(Boolean.class)))
                .thenReturn(mockBoolResponse);

        doReturn(true).when(mockBoolResponse).getBody();

        // Act
        var result = uut.dropBsmPartitions(new ArrayList<String>());

        // Assert
        assertTrue(result);
        verify(mockRestTemplate).exchange(isA(String.class), eq(HttpMethod.POST), any(), eq(Boolean.class));
    }

    @Test
    public void dropBsmPartitions_restClientException() {
        // Arrange
        when(mockRestTemplate.exchange(isA(String.class), eq(HttpMethod.POST), any(), eq(Boolean.class)))
                .thenThrow(new RestClientException(""));

        // Act, Assert
        assertThrows(RestClientException.class, () -> {
            uut.dropBsmPartitions(new ArrayList<String>());
        });
    }
}