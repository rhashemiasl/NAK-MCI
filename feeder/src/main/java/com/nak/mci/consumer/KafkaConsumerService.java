package com.nak.mci.consumer;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Service
@Slf4j
public class KafkaConsumerService implements SourceConsumer{
    @Value("${source.kafka.url}")
    private String sourceKafkaUrl;
    @Value("${source.kafka.source-topic}")
    private String sourceKafkaTopic;
    @Value("${source.kafka.groupId}")
    private String groupId;



    @Value("${destination.kafka.url}")
    private String destinationKafkaURL;
    @Value("${destination.kafka.destination-topic}")
    private String destinationKafkaTopic;


    Vertx vertx;
    KafkaProducer kafkaProducer;




    @Override
    public void runConsumer(Vertx vertx) throws InterruptedException {
        this.vertx = vertx;
        this.kafkaProducer = createKafkaProducer();
        createKafkaConsumer();

    }

    public KafkaProducer createKafkaProducer() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", destinationKafkaURL);
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("acks", "1");

        // use producer for interacting with Apache Kafka
        KafkaProducer<String, String> producer = KafkaProducer.create(vertx, config);
        return producer;
    }


    public io.vertx.kafka.client.consumer.KafkaConsumer createKafkaConsumer() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", sourceKafkaTopic);
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", groupId);
        config.put("auto.offset.reset", "latest");
        // config.put("enable.auto.commit", "false");

        io.vertx.kafka.client.consumer.KafkaConsumer<String, String> consumer = io.vertx.kafka.client.consumer.KafkaConsumer.create(vertx, config);
        consumer.handler(record -> {
            log.info("Processing key=" + record.key() + ",value=" + record.value() +
                    ",partition=" + record.partition() + ",offset=" + record.offset());
            KafkaProducerRecord<String, String> producerRecord = KafkaProducerRecord.create(destinationKafkaTopic, record.value());
            kafkaProducer.write(producerRecord);
        });
        consumer.subscribe(sourceKafkaTopic);
        return consumer;
    }

}
