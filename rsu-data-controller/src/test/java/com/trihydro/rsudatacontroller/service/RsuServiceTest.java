package com.trihydro.rsudatacontroller.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.trihydro.library.helpers.Utility;
import com.trihydro.rsudatacontroller.config.BasicConfiguration;
import com.trihydro.rsudatacontroller.model.RsuTim;
import com.trihydro.rsudatacontroller.process.ProcessFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;

@RunWith(StrictStubs.class)
public class RsuServiceTest {
    @Mock
    ProcessFactory mockProcessFactory;
    ArgumentCaptor<String> factoryArgs = ArgumentCaptor.forClass(String.class);

    @Mock
    Process mockProcess;

    @Mock
    BasicConfiguration mockConfig;

    @Mock
    Utility mockUtility;

    @Mock
    InputStream mockInputStream;

    @InjectMocks
    RsuService uut;

    @Before
    public void initMocks() {
        when(mockConfig.getSnmpRetries()).thenReturn(0);
        when(mockConfig.getSnmpTimeoutSeconds()).thenReturn(10);
        when(mockConfig.getSnmpUserName()).thenReturn("username");
        when(mockConfig.getSnmpAuthPassphrase()).thenReturn("passphrase");
        when(mockConfig.getSnmpAuthProtocol()).thenReturn("protocol");
        when(mockConfig.getSnmpSecurityLevel()).thenReturn("level");

        when(mockProcessFactory.buildAndStartProcess(factoryArgs.capture())).thenReturn(mockProcess);
    }

    @Test
    public void getAllDeliveryStartTimes_success() throws Exception {
        // Arrange
        InputStream output = getInputStream("iso.0.15628.4.1.4.1.7.2 = Hex-STRING: 07 E4 03 14 11 3B",
                "iso.0.15628.4.1.4.1.7.3 = Hex-STRING: 07 E4 03 14 13 1E");
        doReturn(output).when(mockProcess).getInputStream();

        // Act
        List<RsuTim> results = uut.getAllDeliveryStartTimes("0.0.0.0");

        // Assert
        assertEquals(2, results.size());
        assertEquals(2, (int) results.get(0).getIndex());
        assertEquals("2020-03-20 17:59:00", results.get(0).getDeliveryStartTime());
        assertEquals(3, (int) results.get(1).getIndex());
        assertEquals("2020-03-20 19:30:00", results.get(1).getDeliveryStartTime());

        assertEquals(
                "snmpwalk -v 3 -r 0 -t 10 -u username -l level -a protocol -A passphrase 0.0.0.0 1.0.15628.4.1.4.1.7",
                String.join(" ", factoryArgs.getAllValues()));
    }

    @Test
    public void getAllDeliveryStartTimes_single() throws Exception {
        // Arrange
        InputStream output = getInputStream("iso.0.15628.4.1.4.1.7.2 = Hex-STRING: 07 E4 03 14 11 3B ");
        doReturn(output).when(mockProcess).getInputStream();

        // Act
        List<RsuTim> results = uut.getAllDeliveryStartTimes("0.0.0.0");

        // Assert
        assertEquals(1, results.size());
        assertEquals(2, (int) results.get(0).getIndex());
        assertEquals("2020-03-20 17:59:00", results.get(0).getDeliveryStartTime());

        assertEquals(
                "snmpwalk -v 3 -r 0 -t 10 -u username -l level -a protocol -A passphrase 0.0.0.0 1.0.15628.4.1.4.1.7",
                String.join(" ", factoryArgs.getAllValues()));
    }

    @Test
    public void getAllDeliveryStartTimes_none() throws Exception {
        // Arrange
        InputStream output = getInputStream("");
        doReturn(output).when(mockProcess).getInputStream();

        // Act
        List<RsuTim> results = uut.getAllDeliveryStartTimes("0.0.0.0");

        // Assert
        assertEquals(0, results.size());

        assertEquals(
                "snmpwalk -v 3 -r 0 -t 10 -u username -l level -a protocol -A passphrase 0.0.0.0 1.0.15628.4.1.4.1.7",
                String.join(" ", factoryArgs.getAllValues()));
    }

    @Test
    public void getAllDeliveryStartTimes_snmpTimeout() throws Exception {
        // Arrange
        InputStream output = getInputStream("snmpwalk: Timeout");
        doReturn(output).when(mockProcess).getInputStream();

        // Act
        List<RsuTim> results = uut.getAllDeliveryStartTimes("0.0.0.0");

        // Assert
        assertNull(results);
        verify(mockUtility).logWithDate(any());
    }

    @Test(expected = RuntimeException.class)
    public void getAllDeliveryStartTimes_throwsRuntimeException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("unable to find snmpwalk command")).when(mockProcessFactory)
                .buildAndStartProcess(any());

        // Act
        uut.getAllDeliveryStartTimes("0.0.0.0");
    }

    @Test(expected = IOException.class)
    public void getAllDeliveryStartTimes_throwsIOException() throws Exception {
        // Arrange
        // InputStream mockInputStream = mock(InputStream.class);
        doThrow(new IOException("error occurred reading input stream")).when(mockInputStream).read(any(), anyInt(),
                anyInt());
        doReturn(mockInputStream).when(mockProcess).getInputStream();

        // Act
        uut.getAllDeliveryStartTimes("0.0.0.0");
    }

    private InputStream getInputStream(String... lines) {
        String contents = String.join("\n", lines);

        return new ByteArrayInputStream(contents.getBytes());
    }
}