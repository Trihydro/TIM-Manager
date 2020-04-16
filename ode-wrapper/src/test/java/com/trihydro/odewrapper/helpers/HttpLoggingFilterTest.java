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
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.LoggingService;
import com.trihydro.odewrapper.config.BasicConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;

@RunWith(StrictStubs.class)
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
    ServletInputStream mockServletInputStream;
    @Mock
    ServletOutputStream mockServletOutputStream;

    @Mock
    LoggingService mockLoggingService;
    @Mock
    BasicConfiguration mockBasicConfiguration;
    @Mock
    Utility mockUtility;

    @InjectMocks
    HttpLoggingFilter uut;

    @Before
    public void setup() throws ServletException, IOException {
        Enumeration<String> paramNames = Collections.emptyEnumeration();
        doReturn(paramNames).when(mockHttpServletRequest).getParameterNames();
        doReturn(mockServletInputStream).when(mockHttpServletRequest).getInputStream();

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

        // Act
        uut.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        // Assert
        verify(mockLoggingService).LogHttpRequest(any());
    }

    @Test
    public void doFilter_SUCCESS_truncate() throws IOException, ServletException {
        // Arrange
        doReturn("/").when(mockHttpServletRequest).getServletPath();
        doReturn(137).when(mockBasicConfiguration).getHttpLoggingMaxSize();

        // Act
        uut.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        // Assert
        verify(mockLoggingService).LogHttpRequest(any());
        verify(mockUtility).logWithDate(
                "REST Request - [HTTP METHOD:null] [PATH INFO:/] [REQUEST PARAMETERS:{}] [REQUEST BODY:] [REMOTE ADDRESS:null] [RESPONSE CODE:0]");
    }

}