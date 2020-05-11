package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.SDXDecodeRequest;
import com.trihydro.library.model.SDXDecodeResponse;
import com.trihydro.library.model.SdwProps;
import com.trihydro.library.model.SemiDialogID;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;

@PrepareForTest({ SdwService.class })
@RunWith(PowerMockRunner.class)
public class SdwServiceTest extends BaseServiceTest {

    @Mock
    SdwProps mockConfig;

    @Mock
    HttpURLConnection mockUrlConn;

    @Mock
    InputStreamReader mockISReader;

    @Mock
    BufferedReader mockBufferedReader;

    @Mock
    ObjectMapper mockObjMapper;

    @Mock
    OutputStream mockOutputStream;

    @Mock
    Stream<String> mockStringStream;

    @Mock
    Utility mockUtility;

    @Mock
    ResponseEntity<AdvisorySituationDataDeposit[]> mockAsddResponse;

    @Mock
    ResponseEntity<SDXDecodeResponse> mockDecodeResponse;

    @InjectMocks
    SdwService sdwService;

    @Before
    public void setupSubTest() throws SQLException {
        when(mockConfig.getSdwRestUrl()).thenReturn("http://localhost:12230");
        when(mockConfig.getSdwApiKey()).thenReturn("apiKey");
    }

    @Test
    public void getSdwDataByRecordId_nullRecordId() {
        AdvisorySituationDataDeposit asdd = sdwService.getSdwDataByRecordId(null);
        Assert.isNull(asdd, "AdvisorySituationDeposit should be null");
    }

    @Test
    public void getSdwDataByRecordId_nullApiKey() {
        when(mockConfig.getSdwApiKey()).thenReturn(null);
        AdvisorySituationDataDeposit asdd = sdwService.getSdwDataByRecordId("record");
        Assert.isNull(asdd, "AdvisorySituationDeposit should be null");
    }

    @Test
    public void getSdwDataByRecordId_handleException() throws IOException {
        when(mockConfig.getSdwApiKey()).thenReturn("apiKey");
        when(mockUtility.getSdxUrlConnection(isA(String.class), isA(URL.class), isA(String.class)))
                .thenThrow(new IOException());
        AdvisorySituationDataDeposit asdd = sdwService.getSdwDataByRecordId("record");
        Assert.isNull(asdd, "AdvisorySituationDeposit should be null");
    }

    @Test
    public void getSdwDataByRecordId_success() throws IOException, Exception {
        when(mockBufferedReader.readLine()).thenReturn("testValue");
        when(mockConfig.getSdwApiKey()).thenReturn("apiKey");
        when(mockUtility.getSdxUrlConnection(isA(String.class), isA(URL.class), isA(String.class)))
                .thenReturn(mockUrlConn);
        AdvisorySituationDataDeposit asdd_orig = new AdvisorySituationDataDeposit();
        when(mockObjMapper.readValue("testValue", AdvisorySituationDataDeposit.class)).thenReturn(asdd_orig);

        PowerMockito.whenNew(InputStreamReader.class).withAnyArguments().thenReturn(mockISReader);
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(mockBufferedReader);
        PowerMockito.whenNew(ObjectMapper.class).withNoArguments().thenReturn(mockObjMapper);

        AdvisorySituationDataDeposit asdd = sdwService.getSdwDataByRecordId("record");
        verify(mockBufferedReader).readLine();
        verify(mockUtility).getSdxUrlConnection("GET",
                new URL("http://localhost:12230/api/GetDataByRecordId?recordId=record"), "apiKey");
        assertEquals(asdd_orig, asdd);
    }

    @Test
    public void deleteSdxDataBySatRecordId_nullRecordIds() {
        HashMap<Integer, Boolean> results = sdwService.deleteSdxDataBySatRecordId(null);
        verify(mockUtility)
                .logWithDate("Attempting to delete satellite records failed due to no satRecordIds passed in");
        assertNull(results);
    }

    @Test
    public void deleteSdxDataBySatRecordId_emptyRecordIds() {
        HashMap<Integer, Boolean> results = sdwService.deleteSdxDataBySatRecordId(new ArrayList<String>());
        verify(mockUtility)
                .logWithDate("Attempting to delete satellite records failed due to no satRecordIds passed in");
        assertNull(results);
    }

    @Test
    public void deleteSdxDataBySatRecordId_nullApiKey() {
        List<String> satNames = new ArrayList<String>();
        satNames.add("A9184436");
        when(mockConfig.getSdwApiKey()).thenReturn(null);
        HashMap<Integer, Boolean> results = sdwService.deleteSdxDataBySatRecordId(satNames);
        verify(mockUtility).logWithDate("Attempting to delete satellite records failed due to null apiKey");
        assertNull(results);
    }

    @Test
    public void deleteSdxDataBySatRecordId_handleException() throws IOException {
        List<String> satNames = new ArrayList<String>();
        satNames.add("A9184436");
        when(mockUtility.getSdxUrlConnection(isA(String.class), isA(URL.class), isA(String.class)))
                .thenThrow(new IOException());
        HashMap<Integer, Boolean> results = sdwService.deleteSdxDataBySatRecordId(satNames);
        assertNull(results);
    }

    @Test
    public void deleteSdxDataBySatRecordId_success() throws IOException, Exception {
        List<String> satNames = new ArrayList<String>();
        satNames.add("A9184436");
        HashMap<Integer, Boolean> hMap = new HashMap<Integer, Boolean>();
        hMap.put(-1, true);

        when(mockStringStream.collect(any())).thenReturn("{\"-1101625306\":null}");
        when(mockBufferedReader.lines()).thenReturn(mockStringStream);
        PowerMockito.whenNew(InputStreamReader.class).withAnyArguments().thenReturn(mockISReader);
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(mockBufferedReader);
        when(mockUrlConn.getOutputStream()).thenReturn(mockOutputStream);
        when(mockUrlConn.getResponseCode()).thenReturn(200);
        when(mockUtility.getSdxUrlConnection(isA(String.class), isA(URL.class), isA(String.class)))
                .thenReturn(mockUrlConn);
        HashMap<Integer, Boolean> results = sdwService.deleteSdxDataBySatRecordId(satNames);
        assertNotNull(results);
        assertThat(results, IsMapContaining.hasKey(-1101625306));
    }

    @Test
    public void getItisCodesFromAdvisoryMessage_success() {
        // Arrange
        // Response from SDX
        SDXDecodeResponse response = new SDXDecodeResponse();
        String abbDecodedMsg = "<MessageFrame><advisory><SEQUENCE><item><itis>268</itis></item></SEQUENCE>";
        abbDecodedMsg += "<SEQUENCE><item><itis>12619</itis></item></SEQUENCE><SEQUENCE><item><itis>8720</itis></item></SEQUENCE></advisory></MessageFrame>";
        response.setDecodedMessage(abbDecodedMsg);
        response.setMessageType("MessageFrame");

        // Intercept request, returning our fake response
        String url = "http://localhost:12230/api/decode";
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.POST), isA(HttpEntity.class),
                eq(SDXDecodeResponse.class))).thenReturn(mockDecodeResponse);
        when(mockDecodeResponse.getBody()).thenReturn(response);

        // Act
        List<Integer> result = sdwService.getItisCodesFromAdvisoryMessage("AAAAAAAAAAAAAAAAA001F");

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.contains(268));
        assertTrue(result.contains(12619));
        assertTrue(result.contains(8720));
    }

    @Test
    public void getItisCodesFromAdvisoryMessage_realData() {
        // Arrange
        // request contains real, PER-encoded MessageFrame, and response contains the
        // corresponding response from the SDX's /api/decode endpoint
        SDXDecodeRequest request = importJsonObject("/sdxDecodeRequest.json", SDXDecodeRequest.class);
        SDXDecodeResponse response = importJsonObject("/sdxDecodeResponse.json", SDXDecodeResponse.class);

        String url = "http://localhost:12230/api/decode";
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.POST), isA(HttpEntity.class),
                eq(SDXDecodeResponse.class))).thenReturn(mockDecodeResponse);
        when(mockDecodeResponse.getBody()).thenReturn(response);

        // Act
        List<Integer> result = sdwService.getItisCodesFromAdvisoryMessage(request.getEncodedMsg());

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(5895));
        assertTrue(result.contains(5907));
    }

    @Test
    public void getItisCodesFromAdvisoryMessage_nullAdvisoryMessage() {
        // Arrange (if for some reason we get past the guard clause, ensure the REST
        // request doesn't happen)
        String url = "http://localhost:12230/api/decode";
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.POST), isA(HttpEntity.class),
                eq(SDXDecodeResponse.class))).thenReturn(mockDecodeResponse);
        when(mockDecodeResponse.getBody()).thenReturn(null);

        // Act
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            sdwService.getItisCodesFromAdvisoryMessage(null);
        });

        assertEquals("advisoryMessage cannot be null", exception.getMessage());
    }

    @Test
    public void getItisCodesFromAdvisoryMessage_NoMessageFrame() {
        // Arrange (if for some reason we get past the guard clause, ensure the REST
        // request doesn't happen)
        String url = "http://localhost:12230/api/decode";
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.POST), isA(HttpEntity.class),
                eq(SDXDecodeResponse.class))).thenReturn(mockDecodeResponse);
        when(mockDecodeResponse.getBody()).thenReturn(null);

        // Act
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            sdwService.getItisCodesFromAdvisoryMessage("00000000");
        });

        assertEquals("Cannot determine start of MessageFrame", exception.getMessage());
    }

    @Test
    public void getItisCodesFromAdvisoryMessage_handlesNumberFormatException() {
        // Arrange
        // Response from SDX
        SDXDecodeResponse response = new SDXDecodeResponse();
        String abbDecodedMsg = "<MessageFrame><advisory><SEQUENCE><item><itis>268</itis></item></SEQUENCE>";
        abbDecodedMsg += "<SEQUENCE><item><itis>NOTANUMBER</itis></item></SEQUENCE><SEQUENCE><item><itis>8720</itis></item></SEQUENCE></advisory></MessageFrame>";
        response.setDecodedMessage(abbDecodedMsg);
        response.setMessageType("MessageFrame");

        // Intercept request, returning our fake response
        String url = "http://localhost:12230/api/decode";
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.POST), isA(HttpEntity.class),
                eq(SDXDecodeResponse.class))).thenReturn(mockDecodeResponse);
        when(mockDecodeResponse.getBody()).thenReturn(response);

        // Act
        List<Integer> result = sdwService.getItisCodesFromAdvisoryMessage("00000000000000001F");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(268));
        assertTrue(result.contains(8720));
    }

    @Test
    public void getItisCodesFromAdvisoryMessage_handlesRestClientException() {
        // Arrange
        // Response from SDX
        SDXDecodeResponse response = new SDXDecodeResponse();
        String abbDecodedMsg = "<MessageFrame><advisory><SEQUENCE><item><itis>268</itis></item></SEQUENCE>";
        abbDecodedMsg += "<SEQUENCE><item><itis>NOTANUMBER</itis></item></SEQUENCE><SEQUENCE><item><itis>8720</itis></item></SEQUENCE></advisory></MessageFrame>";
        response.setDecodedMessage(abbDecodedMsg);
        response.setMessageType("MessageFrame");

        // Intercept request, returning our fake response
        String url = "http://localhost:12230/api/decode";
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.POST), isA(HttpEntity.class),
                eq(SDXDecodeResponse.class))).thenThrow(new RestClientException("something went wrong..."));
        when(mockDecodeResponse.getBody()).thenReturn(response);

        // Act
        List<Integer> result = sdwService.getItisCodesFromAdvisoryMessage("00000000000000001F");

        // Assert
        assertNull(result);
    }

    @Test
    public void getMsgsForOdeUser_success() {
        // Arrange
        AdvisorySituationDataDeposit[] response = new AdvisorySituationDataDeposit[] {
                new AdvisorySituationDataDeposit() };

        String url = "http://localhost:12230/api/deposited-by-me/156";
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.GET), isA(HttpEntity.class),
                eq(AdvisorySituationDataDeposit[].class))).thenReturn(mockAsddResponse);
        when(mockAsddResponse.getBody()).thenReturn(response);

        // Act
        List<AdvisorySituationDataDeposit> results = sdwService.getMsgsForOdeUser(SemiDialogID.AdvSitDataDep);

        // Assert
        assertEquals(1, results.size());
        verify(mockRestTemplate).exchange(eq(url), eq(HttpMethod.GET), isA(HttpEntity.class),
                eq(AdvisorySituationDataDeposit[].class));
    }

    @Test
    public void getMsgsForOdeUser_handlesRestClientException() {
        // Arrange
        String url = "http://localhost:12230/api/deposited-by-me/156";
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.GET), isA(HttpEntity.class),
                eq(AdvisorySituationDataDeposit[].class)))
                        .thenThrow(new RestClientException("something went wrong..."));

        // Act
        List<AdvisorySituationDataDeposit> results = sdwService.getMsgsForOdeUser(SemiDialogID.AdvSitDataDep);

        // Assert
        assertNull(results);
    }
}