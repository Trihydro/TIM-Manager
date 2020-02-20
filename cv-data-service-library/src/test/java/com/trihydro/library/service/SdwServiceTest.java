package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.ConfigProperties;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.util.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Utility.class, SdwService.class })
public class SdwServiceTest {

    @Mock
    ConfigProperties mockConfig;

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

    @InjectMocks
    SdwService sdwService;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Utility.class);
        Mockito.when(mockConfig.getSdwRestUrl()).thenReturn("http://localhost:12230");
        Mockito.when(mockConfig.getSdwApiKey()).thenReturn("apiKey");
    }

    @Test
    public void getSdwDataByRecordId_nullRecordId() {
        AdvisorySituationDataDeposit asdd = sdwService.getSdwDataByRecordId(null);
        Assert.isNull(asdd);
    }

    @Test
    public void getSdwDataByRecordId_nullApiKey() {
        Mockito.when(mockConfig.getSdwApiKey()).thenReturn(null);
        AdvisorySituationDataDeposit asdd = sdwService.getSdwDataByRecordId("record");
        Assert.isNull(asdd);
    }

    @Test
    public void getSdwDataByRecordId_handleException() throws IOException {
        Mockito.when(mockConfig.getSdwApiKey()).thenReturn("apiKey");
        Mockito.when(Utility.getSdxUrlConnection(isA(String.class), isA(URL.class), isA(String.class)))
                .thenThrow(new IOException());
        AdvisorySituationDataDeposit asdd = sdwService.getSdwDataByRecordId("record");
        Assert.isNull(asdd);
    }

    @Test
    public void getSdwDataByRecordId_success() throws IOException, Exception {
        Mockito.when(mockBufferedReader.readLine()).thenReturn("testValue");
        Mockito.when(mockConfig.getSdwApiKey()).thenReturn("apiKey");
        Mockito.when(Utility.getSdxUrlConnection(isA(String.class), isA(URL.class), isA(String.class)))
                .thenReturn(mockUrlConn);
        AdvisorySituationDataDeposit asdd_orig = new AdvisorySituationDataDeposit();
        Mockito.when(mockObjMapper.readValue("testValue", AdvisorySituationDataDeposit.class)).thenReturn(asdd_orig);

        PowerMockito.whenNew(InputStreamReader.class).withAnyArguments().thenReturn(mockISReader);
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(mockBufferedReader);
        PowerMockito.whenNew(ObjectMapper.class).withNoArguments().thenReturn(mockObjMapper);

        AdvisorySituationDataDeposit asdd = sdwService.getSdwDataByRecordId("record");
        verify(mockBufferedReader).readLine();
        PowerMockito.verifyStatic();
        Utility.getSdxUrlConnection("GET", new URL("http://localhost:12230/api/GetDataByRecordId?recordId=record"),
                "apiKey");
        assertEquals(asdd_orig, asdd);
    }

    @Test
    public void deleteSdxDataBySatRecordId_nullRecordIds() {
        HashMap<Integer, Boolean> results = sdwService.deleteSdxDataBySatRecordId(null);
        PowerMockito.verifyStatic();
        Utility.logWithDate("Attempting to delete satellite records failed due to no satRecordIds passed in");
        assertNull(results);
    }

    @Test
    public void deleteSdxDataBySatRecordId_emptyRecordIds() {
        HashMap<Integer, Boolean> results = sdwService.deleteSdxDataBySatRecordId(new ArrayList<String>());
        PowerMockito.verifyStatic();
        Utility.logWithDate("Attempting to delete satellite records failed due to no satRecordIds passed in");
        assertNull(results);
    }

    @Test
    public void deleteSdxDataBySatRecordId_nullApiKey() {
        List<String> satNames = new ArrayList<String>();
        satNames.add("A9184436");
        Mockito.when(mockConfig.getSdwApiKey()).thenReturn(null);
        HashMap<Integer, Boolean> results = sdwService.deleteSdxDataBySatRecordId(satNames);
        PowerMockito.verifyStatic();
        Utility.logWithDate("Attempting to delete satellite records failed due to null apiKey");
        assertNull(results);
    }

    @Test
    public void deleteSdxDataBySatRecordId_handleException() throws IOException {
        List<String> satNames = new ArrayList<String>();
        satNames.add("A9184436");
        Mockito.when(Utility.getSdxUrlConnection(isA(String.class), isA(URL.class), isA(String.class)))
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

        Mockito.when(mockStringStream.collect(isA(Collector.class))).thenReturn("{\"-1101625306\":null}");
        Mockito.when(mockBufferedReader.lines()).thenReturn(mockStringStream);
        PowerMockito.whenNew(InputStreamReader.class).withAnyArguments().thenReturn(mockISReader);
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(mockBufferedReader);
        Mockito.when(mockUrlConn.getOutputStream()).thenReturn(mockOutputStream);
        Mockito.when(mockUrlConn.getResponseCode()).thenReturn(200);
        Mockito.when(Utility.getSdxUrlConnection(isA(String.class), isA(URL.class), isA(String.class)))
                .thenReturn(mockUrlConn);
        HashMap<Integer, Boolean> results = sdwService.deleteSdxDataBySatRecordId(satNames);
        assertNotNull(results);
        assertThat(results, IsMapContaining.hasKey(-1101625306));
    }
}