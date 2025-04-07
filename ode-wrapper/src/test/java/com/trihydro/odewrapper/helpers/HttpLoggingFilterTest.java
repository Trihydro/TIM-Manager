package com.trihydro.odewrapper.helpers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.HttpLoggingModel;
import com.trihydro.library.service.LoggingService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.model.BufferedRequestWrapper;
import com.trihydro.odewrapper.model.BufferedResponseWrapper;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class HttpLoggingFilterTest {
    @Mock
    HttpServletRequest mockHttpServletRequest;
    @Mock
    HttpServletResponse mockHttpServletResponse;
    @Mock
    FilterChain mockFilterChain;
    @Mock
    FilterConfig mockFilterConfig;

    @Mock
    LoggingService mockLoggingService;
    @Mock
    BasicConfiguration mockBasicConfiguration;
    @Mock
    Utility mockUtility;

    @Mock
    BufferedResponseWrapperFactory mockBufferedResponseWrapperFactory;
    @Mock
    BufferedResponseWrapper mockBufferedResponseWrapper;
    @Mock
    BufferedRequestWrapperFactory mockBufferedRequestWrapperFactory;
    @Mock
    BufferedRequestWrapper mockBufferedRequestWrapper;

    @InjectMocks
    HttpLoggingFilter uut;

    @BeforeEach
    public void setup() throws ServletException, IOException {
        Enumeration<String> paramNames = Collections.emptyEnumeration();
        doReturn(paramNames).when(mockHttpServletRequest).getParameterNames();
        doReturn(mockBufferedResponseWrapper).when(mockBufferedResponseWrapperFactory)
                .getBufferedResponseWrapper(any());
        doReturn(mockBufferedRequestWrapper).when(mockBufferedRequestWrapperFactory).getBufferedRequestWrapper(any());

        uut.init(mockFilterConfig);
    }

    @Test
    public void doFilter_skip_swagger() throws IOException, ServletException {
        // Arrange
        doReturn("swagger").when(mockHttpServletRequest).getServletPath();

        // Act
        uut.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        // Assert
        verifyNoInteractions(mockLoggingService);
    }

    @Test
    public void doFilter_skip_apiDocs() throws IOException, ServletException {
        // Arrange
        doReturn("api-docs").when(mockHttpServletRequest).getServletPath();

        // Act
        uut.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        // Assert
        verifyNoInteractions(mockLoggingService);
    }

    @Test
    public void doFilter_SUCCESS() throws IOException, ServletException {
        // Arrange
        doReturn("/").when(mockHttpServletRequest).getServletPath();
        doReturn(2000).when(mockBasicConfiguration).getHttpLoggingMaxSize();
        doReturn("log body").when(mockBufferedRequestWrapper).getRequestBody();
        doReturn(200).when(mockBufferedResponseWrapper).getStatus();
        doReturn("response").when(mockBufferedResponseWrapper).getContent();

        // Act
        uut.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        // Assert
        verify(mockLoggingService).LogHttpRequest(any());
    }

    @Test
    public void doFilter_SUCCESS_truncate() throws IOException, ServletException {
        // Arrange
        doReturn("/").when(mockHttpServletRequest).getServletPath();
        doReturn(150).when(mockBasicConfiguration).getHttpLoggingMaxSize();
        doReturn("this is a long request body to be truncated").when(mockBufferedRequestWrapper).getRequestBody();
        doReturn(200).when(mockBufferedResponseWrapper).getStatus();
        doReturn("this is a longer content").when(mockBufferedResponseWrapper).getContent();

        // Act
        uut.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        // Assert
        HttpLoggingModel expectedLoggingModel = new HttpLoggingModel();
        expectedLoggingModel.setRequest("REST Request - [HTTP METHOD:null] [PATH INFO:/] [REQUEST PARAMETERS:{}] [REQUEST BODY:this is a long request...] [RESPONSE CODE:200] [RESPONSE:thi...]");
        verify(mockLoggingService).LogHttpRequest(
                ArgumentMatchers.argThat(argument -> argument.getRequest().equals(expectedLoggingModel.getRequest()))
        );
    }

}