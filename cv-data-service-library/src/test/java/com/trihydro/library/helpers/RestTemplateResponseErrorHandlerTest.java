package com.trihydro.library.helpers;

import java.io.IOException;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@ExtendWith(MockitoExtension.class)
public class RestTemplateResponseErrorHandlerTest {

    private RestTemplateResponseErrorHandler uut = new RestTemplateResponseErrorHandler();

    @Test
    public void hasError_FALSE() throws IOException {
        // Arrange
        byte[] body = "Hello World".getBytes();
        var mockClientHttpResponse = new MockClientHttpResponse(body, HttpStatus.OK);

        // Act
        var data = uut.hasError(mockClientHttpResponse);

        // Assert
        Assertions.assertFalse(data);
    }

    @Test
    public void hasError_TRUE_ClientError() throws IOException {
        // Arrange
        byte[] body = "Hello World".getBytes();
        var mockClientHttpResponse = new MockClientHttpResponse(body, HttpStatus.I_AM_A_TEAPOT);

        // Act
        var data = uut.hasError(mockClientHttpResponse);

        // Assert
        Assertions.assertTrue(data);
    }

    @Test
    public void hasError_TRUE_ServerError() throws IOException {
        // Arrange
        byte[] body = "Hello World".getBytes();
        var mockClientHttpResponse = new MockClientHttpResponse(body, HttpStatus.INTERNAL_SERVER_ERROR);

        // Act
        var data = uut.hasError(mockClientHttpResponse);

        // Assert
        Assertions.assertTrue(data);
    }

    @Test
    public void handleError_Client() throws IOException {
        // Arrange
        byte[] body = "Hello World".getBytes();
        var mockClientHttpResponse = new MockClientHttpResponse(body, HttpStatus.I_AM_A_TEAPOT);

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> uut.handleError(mockClientHttpResponse));
    }

    @Test
    public void handleError_Server() throws IOException {
        // Arrange
        byte[] body = "Hello World".getBytes();
        var mockClientHttpResponse = new MockClientHttpResponse(body, HttpStatus.INTERNAL_SERVER_ERROR);

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> uut.handleError(mockClientHttpResponse));
    }

    @Test
    public void defaultHandleError_Throws_Client() {
        // Arrange
        byte[] body = "Hello World".getBytes();
        var mockClientHttpResponse = new MockClientHttpResponse(body, HttpStatus.I_AM_A_TEAPOT);
        DefaultResponseErrorHandler def_responseHandler = new DefaultResponseErrorHandler();

        // Act / Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> def_responseHandler.handleError(mockClientHttpResponse));
    }

    @Test
    public void defaultHandleError_Throws_Server() {
        // Arrange
        byte[] body = "Hello World".getBytes();
        var mockClientHttpResponse = new MockClientHttpResponse(body, HttpStatus.INTERNAL_SERVER_ERROR);
        DefaultResponseErrorHandler def_responseHandler = new DefaultResponseErrorHandler();

        // Act / Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> def_responseHandler.handleError(mockClientHttpResponse));
    }
}
