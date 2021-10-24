package com.nak.mci.consumer;

import io.vertx.core.Vertx;

public interface SourceConsumer {
    void runConsumer(Vertx vertx) throws InterruptedException;
}
