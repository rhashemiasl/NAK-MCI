package nak.mci;

import io.vertx.core.Vertx;
import nak.mci.verticles.FetcherVerticle;
import nak.mci.verticles.KafkaVerticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@Configuration
public class VertxSpringApplication {

    @Autowired
    private KafkaVerticle kafkaVerticle;

    @Autowired
    private FetcherVerticle fetcherVerticle;


    public static void main(String[] args) {
        SpringApplication.run(VertxSpringApplication.class, args);
    }

    @PostConstruct
    public void deployVerticle() {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(kafkaVerticle);
        vertx.deployVerticle(fetcherVerticle);
    }
}
