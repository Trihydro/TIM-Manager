package com.trihydro.library.factory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.trihydro.library.helpers.Utility;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KafkaFactory {
    private Utility utility;

    @Autowired
    public KafkaFactory(Utility _utility) {
        utility = _utility;
    }

    /**
     * Creates a Kafka consumer that has a key.deserializer and value.deserializer
     * of type StringDeserializer
     * 
     * @param host          URI or IP of a broker within the Kafka Cluster
     * @param consumerGroup Name of group this consumer should join
     * @param topic         Topic to subscribe to
     * @return A new string consumer
     */
    public Consumer<String, String> createStringConsumer(String host, String consumerGroup, String topic) {
        return createStringConsumer(host, consumerGroup, Arrays.asList(topic));
    }

    /**
     * Creates a Kafka consumer that has a key.deserializer and value.deserializer
     * of type StringDeserializer
     * 
     * @param host          URI or IP of a broker within the Kafka Cluster
     * @param consumerGroup Name of group this consumer should join
     * @param topics        Topics to subscribe to
     * @return A new string consumer
     */
    public Consumer<String, String> createStringConsumer(String host, String consumerGroup, List<String> topics) {
        var props = new Properties();
        props.put("bootstrap.servers", host);
        props.put("group.id", consumerGroup);
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        var consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(topics);

        utility.logWithDate(String.format("Created consumer for consumer group %s, subscribed to topic(s) %s",
                consumerGroup, String.join(", ", topics)));

        return consumer;
    }

    /**
     * Creates a Kafka producer that has a key.deserializer and value.deserializer
     * of type StringDeserializer
     * 
     * @param host URI or IP of a broker within the Kafka Cluster
     * @return A new string producer
     */
    public Producer<String, String> createStringProducer(String host) {
        Properties props = new Properties();
		props.put("bootstrap.servers", host);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        
        return new KafkaProducer<String, String>(props);
    }
}