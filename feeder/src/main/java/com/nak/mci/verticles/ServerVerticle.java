package com.nak.mci.verticles;

import com.nak.mci.consumer.ConsumerFactory;
import io.vertx.core.AbstractVerticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServerVerticle extends AbstractVerticle {





   ConsumerFactory consumerFactory;

   @Autowired
    public ServerVerticle(ConsumerFactory consumerFactory) {
        this.consumerFactory = consumerFactory;
    }

    @Override
    public void start() throws Exception {
        super.start();
        System.out.println("Inside Start");
        consumerFactory.runConsumer(vertx);
    }

}
