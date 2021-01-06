package com.trihydro.certexpiration.app;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.trihydro.library.factory.KafkaFactory;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CertExpirationConsumerTest {
    private static final String TOPIC = "topic";
    private static final String PRODUCERTOPIC = "producerTopic";
    private static final int PARTITION = 0;
    private static final String EXPRECORD = "{\"expirationDate\":\"2020-10-20T16:26:07.000Z\",\"packetID\":\"3C8E8DF2470B1A772E\",\"requiredExpirationDate\":\"2020-11-05T20:57:26.037Z\",\"startDateTime\":\"2020-10-14T15:37:26.037Z\"}";
    private static final long TIMESTAMP = System.currentTimeMillis() - 5000;// 5 seconds ago
    private static final long CHECKSUM = -1l;
    private static final int KEYSIZE = -1;
    private static final int VALUESIZE = 32;
    private static final String CERTTOPIC = "certTopic";
    private static final int OFFSET = 0;

    @Mock
    private CertExpirationConfiguration mockConfigProperties;
    @Mock
    private Utility mockUtility;
    @Mock
    private EmailHelper mockEmailHelper;
    @Mock
    private LoopController mockLoopController;
    @Mock
    private KafkaFactory mockKafkaFactory;

    private MockConsumer<String, String> mockConsumer;
    private MockProducer<String, String> mockProducer;

    @InjectMocks
    private CertExpirationConsumer uut;

    @BeforeEach
    public void setUp() throws Exception {
        doReturn(CERTTOPIC).when(mockConfigProperties).getDepositTopic();
        doReturn(PRODUCERTOPIC).when(mockConfigProperties).getProducerTopic();

        // init mockConsumer, setup a poll
        var record = new ConsumerRecord<>(TOPIC, PARTITION, OFFSET, TIMESTAMP, TimestampType.NO_TIMESTAMP_TYPE,
                CHECKSUM, KEYSIZE, VALUESIZE, "Key", EXPRECORD);
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        mockConsumer.schedulePollTask(() -> {
            mockConsumer.subscribe(Collections.singleton(TOPIC));
            mockConsumer.rebalance(Collections.singletonList(new TopicPartition(TOPIC, PARTITION)));
            mockConsumer.addRecord(record);
        });

        // set offset start
        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        TopicPartition tp = new TopicPartition(TOPIC, PARTITION);
        startOffsets.put(tp, 0L);
        mockConsumer.updateBeginningOffsets(startOffsets);

        // init mockProducer
        mockProducer = new MockProducer<>();

        // inject consumer to factory
        doReturn(mockConsumer).when(mockKafkaFactory).createStringConsumer(any(), any(), anyString());
        doReturn(mockProducer).when(mockKafkaFactory).createStringProducer(any());
        // loop controller returns true, then false to kick out
        when(mockLoopController.loop()).thenReturn(true).thenReturn(false);
    }

    public void configureConsumerException(String msg) {
        Mockito.reset(mockKafkaFactory);
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        mockConsumer.schedulePollTask(() -> {
            mockConsumer.setPollException(new KafkaException(msg));
        });

        doReturn(mockConsumer).when(mockKafkaFactory).createStringConsumer(any(), any(), anyString());
        doReturn(mockProducer).when(mockKafkaFactory).createStringProducer(any());
    }

    @Test
    public void startKafkaConsumer_SUCCESS() throws Exception {
        // Act
        uut.startKafkaConsumer();

        // Assert
        Assertions.assertEquals(1, mockProducer.history().size());
        
        verify(mockUtility).logWithDate("starting..............");
        verify(mockUtility).logWithDate("Found topic topic, submitting to producerTopic for later consumption");

        verifyNoMoreInteractions(mockUtility);
        Assertions.assertTrue(mockConsumer.closed());
        Assertions.assertTrue(mockProducer.closed());
    }

    @Test
    public void startKafkaConsumer_EXCEPTION() throws Exception {
        // Arrange
        configureConsumerException("Network error");

        // Act
        Exception ex = assertThrows(KafkaException.class, () -> uut.startKafkaConsumer());

        // Assert
        Assertions.assertEquals("Network error", ex.getMessage());
        verify(mockUtility).logWithDate("starting..............");
        
        verify(mockUtility).logWithDate("Network error");
        verify(mockEmailHelper).ContainerRestarted(any(), any(), any(), any(), any());

        verifyNoMoreInteractions(mockUtility);
        Assertions.assertTrue(mockConsumer.closed());
        Assertions.assertTrue(mockProducer.closed());
    }

    @Test
    public void startKafkaConsumer_DOUBLEEXCEPTION() throws Exception {
        // Arrange
        configureConsumerException("Network error");
        doThrow(new MessagingException("Mail Exception")).when(mockEmailHelper).ContainerRestarted(any(), any(), any(), any(), any());

        // Act
        Exception ex = assertThrows(MessagingException.class, () -> uut.startKafkaConsumer());

        // Assert
        Assertions.assertEquals("Mail Exception", ex.getMessage());

        verify(mockUtility).logWithDate("starting..............");

        verify(mockUtility).logWithDate("Network error");
        verify(mockEmailHelper).ContainerRestarted(any(), any(), any(), any(), any());

        verifyNoMoreInteractions(mockUtility);
        Assertions.assertTrue(mockConsumer.closed());
        Assertions.assertTrue(mockProducer.closed());
    }
}
