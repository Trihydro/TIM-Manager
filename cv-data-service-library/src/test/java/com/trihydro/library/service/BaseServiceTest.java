package com.trihydro.library.service;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

import com.google.gson.Gson;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Ignore
@ExtendWith(MockitoExtension.class)
public class BaseServiceTest {
    @Mock
    protected RestTemplate mockRestTemplate;
    @Mock
    protected RestTemplateProvider mockRestTemplateProvider;

    @BeforeEach
    public void setup() throws SQLException {
        when(mockRestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);
    }

    protected HttpHeaders getDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @SuppressWarnings("unchecked")
    protected <T> HttpEntity<T> getEntity(Object body, Class<T> clazz) {
        HttpEntity<T> entity = new HttpEntity<T>((T) body, getDefaultHeaders());
        return entity;
    }

    @SuppressWarnings("unchecked")
    protected <T> HttpEntity<T> getEntity(Object body, Class<T> clazz, HttpHeaders customHeaders) {
        HttpEntity<T> entity = new HttpEntity<T>((T) body, customHeaders);
        return entity;
    }

    protected <T> T importJsonObject(String fileName, Class<T> clazz) {
        InputStream is = BaseServiceTest.class.getResourceAsStream(fileName);
        InputStreamReader isr = new InputStreamReader(is);

        Gson gson = new Gson();
        T data = gson.fromJson(isr, clazz);

        try {
            isr.close();
        } catch (IOException ex) {

        }

        return data;
    }
}