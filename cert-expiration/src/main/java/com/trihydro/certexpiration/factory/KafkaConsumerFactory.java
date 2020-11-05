package com.trihydro.certexpiration.factory;

import java.util.Arrays;
import java.util.Properties;

import com.trihydro.certexpiration.config.CertExpirationConfiguration;
import com.trihydro.library.helpers.Utility;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerFactory {
    private CertExpirationConfiguration configProperties;
    private Utility utility;

    @Autowired
    public KafkaConsumerFactory(CertExpirationConfiguration _configProperties, Utility _utility) {
        configProperties = _configProperties;
        utility = _utility;
    }

    public Consumer<String, String> createConsumer() {
        String endpoint = configProperties.getKafkaHostServer() + ":9092";

        // Properties for the kafka topic
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", endpoint);
        consumerProps.put("group.id", configProperties.getDepositGroup());
        // turn off auto commit, so we can handle the race condition
        consumerProps.put("enable.auto.commit", "false");
        consumerProps.put("session.timeout.ms", "30000");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> stringConsumer = new KafkaConsumer<String, String>(consumerProps);

        // subscribe
        String consumerTopic = configProperties.getDepositTopic();
        stringConsumer.subscribe(Arrays.asList(consumerTopic));
        utility.logWithDate("Subscribed to topic " + consumerTopic);
        return stringConsumer;
    }
}
