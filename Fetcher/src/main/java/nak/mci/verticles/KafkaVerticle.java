package nak.mci.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class KafkaVerticle extends AbstractVerticle {


    @Value("${source.kafka.InvalidUrl-topic}")
    private String invalidTopic;

    @Value("${source.kafka.unprocessed-topic}")
    private String unprocessedTopic;

    @Value("${source.kafka.url}")
    private String kafkaURL;

    @Value("${source.kafka.groupId}")
    private String groupId;

    @Value("${source.kafka.source-topic}")
    private String urlsTopic;


    KafkaProducer kafkaProducer;

    public KafkaProducer createKafkaProducer() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", kafkaURL);
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("acks", "1");

        // use producer for interacting with Apache Kafka
        KafkaProducer<String, String> producer = KafkaProducer.create(vertx, config);
        return producer;
    }


    /*
    setting up a kafka consumer to received new event.
     once a new message received, it will send to vertx event bus.
     */
    public KafkaConsumer createKafkaConsumer() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", kafkaURL);
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", groupId);
        config.put("auto.offset.reset", "latest");
        KafkaConsumer<String, String> consumer = KafkaConsumer.create(vertx, config);
        consumer.handler(record -> {
            log.info("Processing key=" + record.key() + ",value=" + record.value() +
                    ",partition=" + record.partition() + ",offset=" + record.offset());
            vertx.eventBus().send("fetch.link", record.value());
        });
        consumer.subscribe(urlsTopic);
        return consumer;
    }


    @Override
    public void start() throws Exception {
        log.info("Start Processing");
        kafkaProducer = createKafkaProducer();
        createKafkaConsumer();
        consumeUnprocessedLink();
        consumeInvalidLink();
    }


    /*
    persist unprocessed urls in a kafka topic
     */
    private void consumeUnprocessedLink() {
        vertx.eventBus().localConsumer("unprocessed.link", message -> {
            String urlLink = (String) message.body();

            KafkaProducerRecord<String, String> record =
                    KafkaProducerRecord.create(unprocessedTopic, urlLink);

            kafkaProducer.write(record);
        });
    }
    /*
        persist invalid urls in a kafka topic
    */
    private void consumeInvalidLink() {
        vertx.eventBus().localConsumer("invalid.link", message -> {
            String urlLink = (String) message.body();

            KafkaProducerRecord<String, String> record =
                    KafkaProducerRecord.create(invalidTopic, urlLink);

            kafkaProducer.write(record);
        });
    }
}