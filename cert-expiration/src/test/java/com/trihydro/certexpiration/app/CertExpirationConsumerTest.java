package com.trihydro.certexpiration.app;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import javax.mail.MessagingException;

import com.trihydro.certexpiration.config.CertExpirationConfiguration;
import com.trihydro.certexpiration.controller.LoopController;
import com.trihydro.certexpiration.factory.KafkaConsumerFactory;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.ActiveTimService;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CertExpirationConsumerTest {
    private static final String TOPIC = "topic";
    private static final int PARTITION = 0;
    private static final String EXPRECORD = "{\"expirationDate\":\"2020-10-20T16:26:07.000Z\",\"packetID\":\"3C8E8DF2470B1A772E\",\"requiredExpirationDate\":\"2020-11-05T20:57:26.037Z\",\"startDateTime\":\"2020-10-14T15:37:26.037Z\"}";
    private static final String PACKETID = "3C8E8DF2470B1A772E";
    private static final String STARTDATE = "2020-10-14T15:37:26.037Z";
    private static final String EXPDATE = "2020-10-20T16:26:07.000Z";
    private static final String MINEXP = "27-OCT-20 06.21.00.000 PM";

    @Mock
    private CertExpirationConfiguration mockConfigProperties;
    @Mock
    private Utility mockUtility;
    @Mock
    private ActiveTimService mockAts;
    @Mock
    private EmailHelper mockEmailHelper;
    @Mock
    private LoopController mockLoopController;
    @Mock
    private KafkaConsumerFactory mockKafkaConsumerFactory;

    private MockConsumer<String, String> mockConsumer;

    @InjectMocks
    private CertExpirationConsumer uut;

    @BeforeEach
    public void setUp() throws Exception {
        // init mockConsumer, setup a poll
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        mockConsumer.schedulePollTask(() -> {
            mockConsumer.subscribe(Collections.singleton(TOPIC));
            mockConsumer.rebalance(Collections.singletonList(new TopicPartition(TOPIC, PARTITION)));
            // if (!badRecord) {
            mockConsumer.addRecord(new ConsumerRecord<>(TOPIC, PARTITION, 0, "Key", EXPRECORD));
            // } else {
            // mockConsumer.addRecord(new ConsumerRecord<>(TOPIC, PARTITION, 0, "Key",
            // "asdf"));
            // }
        });

        // set offset start
        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        TopicPartition tp = new TopicPartition(TOPIC, PARTITION);
        startOffsets.put(tp, 0L);
        mockConsumer.updateBeginningOffsets(startOffsets);

        // inject consumer to factory
        doReturn(mockConsumer).when(mockKafkaConsumerFactory).createConsumer();

        // loop controller returns true, then false to kick out
        when(mockLoopController.loop()).thenReturn(true).thenReturn(false);
    }

    @Test
    public void startKafkaConsumer_SUCCESS() throws Exception {
        // Arrange
        doReturn(true).when(mockAts).updateActiveTimExpiration(any(), any(), any());
        doReturn(MINEXP).when(mockAts).getMinExpiration(any(), any(), any());

        // Act
        uut.startKafkaConsumer();

        // Assert
        verify(mockAts).getMinExpiration(PACKETID, STARTDATE, EXPDATE);
        verify(mockAts).updateActiveTimExpiration(PACKETID, STARTDATE, MINEXP);
        verify(mockUtility).logWithDate("starting..............");
        verify(mockUtility).logWithDate(String.format("Consumed from expiration topic: %s", EXPRECORD));
        verify(mockUtility).logWithDate("Successfully updated expiration date");
        verifyNoMoreInteractions(mockUtility);
        verifyNoMoreInteractions(mockAts);
        Assertions.assertTrue(mockConsumer.closed());
    }

    @Test
    public void startKafkaConsumer_FAIL() throws Exception {
        // Arrange
        doReturn(false).when(mockAts).updateActiveTimExpiration(any(), any(), any());
        doReturn(MINEXP).when(mockAts).getMinExpiration(any(), any(), any());

        // Act
        uut.startKafkaConsumer();

        // Assert
        verify(mockAts).getMinExpiration(PACKETID, STARTDATE, EXPDATE);
        verify(mockAts).updateActiveTimExpiration(PACKETID, STARTDATE, MINEXP);
        verify(mockUtility).logWithDate("starting..............");
        verify(mockUtility).logWithDate(String.format("Consumed from expiration topic: %s", EXPRECORD));
        verify(mockUtility).logWithDate(String.format("Failed to update expiration for data: %s", EXPRECORD));

        String body = "The CertExpirationConsumer failed attempting to update an ActiveTim record";
        body += "<br/>";
        body += "The associated expiration topic record is: <br/>";
        body += EXPRECORD;
        verify(mockEmailHelper).SendEmail(mockConfigProperties.getAlertAddresses(), null,
                "CertExpirationConsumer Failed To Update ActiveTim", body, mockConfigProperties.getMailPort(),
                mockConfigProperties.getMailHost(), mockConfigProperties.getFromEmail());

        verifyNoMoreInteractions(mockUtility);
        verifyNoMoreInteractions(mockAts);
        Assertions.assertTrue(mockConsumer.closed());
    }

    @Test
    public void startKafkaConsumer_EXCEPTION() throws Exception {
        // Arrange
        String exMessage = "Big error";
        doReturn(MINEXP).when(mockAts).getMinExpiration(any(), any(), any());
        doThrow(new NullPointerException(exMessage)).when(mockAts).updateActiveTimExpiration(any(), any(), any());

        // Act
        Exception ex = assertThrows(Exception.class, () -> uut.startKafkaConsumer());

        // Assert
        verify(mockUtility).logWithDate("starting..............");
        verify(mockUtility).logWithDate(String.format("Consumed from expiration topic: %s", EXPRECORD));
        verify(mockAts).getMinExpiration(PACKETID, STARTDATE, EXPDATE);
        verify(mockAts).updateActiveTimExpiration(PACKETID, STARTDATE, MINEXP);

        verify(mockUtility).logWithDate(exMessage);
        String body = "The CertExpirationConsumer failed attempting to consume records";
        body += ". <br/>Exception message: ";
        body += ex.getMessage();
        body += "<br/>Stacktrace: ";
        body += ExceptionUtils.getStackTrace(ex);
        verify(mockEmailHelper).SendEmail(mockConfigProperties.getAlertAddresses(), null,
                "CertExpirationConsumer Failed", body, mockConfigProperties.getMailPort(),
                mockConfigProperties.getMailHost(), mockConfigProperties.getFromEmail());

        verifyNoMoreInteractions(mockUtility);
        verifyNoMoreInteractions(mockAts);
        Assertions.assertTrue(mockConsumer.closed());
    }

    @Test
    public void startKafkaConsumer_DOUBLEEXCEPTION() throws Exception {
        // Arrange
        String exMessage = "Big error";
        doThrow(new NullPointerException(exMessage)).when(mockAts).updateActiveTimExpiration(any(), any(), any());
        doThrow(new MessagingException("Mail Exception")).when(mockEmailHelper).SendEmail(any(), any(), any(), any(),
                any(), any(), any());

        // Act
        Exception ex = assertThrows(Exception.class, () -> uut.startKafkaConsumer());

        // Assert
        verify(mockUtility).logWithDate("starting..............");
        verify(mockUtility).logWithDate(String.format("Consumed from expiration topic: %s", EXPRECORD));

        verify(mockUtility).logWithDate(exMessage);
        String body = "The CertExpirationConsumer failed attempting to consume records";
        body += ". <br/>Exception message: ";
        body += ex.getMessage();
        body += "<br/>Stacktrace: ";
        body += ExceptionUtils.getStackTrace(ex);
        verify(mockEmailHelper).SendEmail(mockConfigProperties.getAlertAddresses(), null,
                "CertExpirationConsumer Failed", body, mockConfigProperties.getMailPort(),
                mockConfigProperties.getMailHost(), mockConfigProperties.getFromEmail());

        verify(mockUtility).logWithDate("CertExpirationConsumer failed, then failed to send email");
        verifyNoMoreInteractions(mockUtility);
        Assertions.assertTrue(mockConsumer.closed());
    }
}
