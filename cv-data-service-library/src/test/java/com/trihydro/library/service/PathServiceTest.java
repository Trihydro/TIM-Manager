package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

@RunWith(PowerMockRunner.class)

public class PathServiceTest extends BaseServiceTest {

    @Test
    public void insertPath() {
        // Arrange
        String url = "null/path/add-path";
        HttpEntity<String> entity = getEntity(null, String.class);
        doReturn(mockResponseEntityLong).when(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);

        // Act
        Long data = PathService.insertPath();

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);
        assertEquals(new Long(1), data);
    }
}