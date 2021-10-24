package com.nak.mci.consumer;

import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConsumerFactory {


    @Value("${source.type}")
    private String sourceType;

    @Autowired
    FileConsumerService fileConsumerService;

    @Autowired
    KafkaConsumerService kafkaConsumerService;

    public void runConsumer(Vertx vertx) throws InterruptedException {
        if(sourceType.equals("File"))
            fileConsumerService.runConsumer(vertx);
        else
            kafkaConsumerService.runConsumer(vertx);
    }
}
