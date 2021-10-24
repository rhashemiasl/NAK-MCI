package nak.mci.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import nak.mci.service.WebPageContentFetcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Component
@Slf4j
public class FetcherVerticle extends AbstractVerticle {




    @Value("${mongo.database.url}")
    String mongoURL;
    @Value("${mongo.database.name}")
    String mongoDBName;

    @Autowired
    WebPageContentFetcherService webPageContentFetcherService;

    MongoClient mongoClient;

    public MongoClient createNonSharedMongoDB(Vertx vertx, JsonObject config) {
        return  MongoClient.create(vertx, config);
    }


    @Override
    public void start() throws Exception {
        super.start();
        JsonObject mongoconfig = new JsonObject()
                .put("connection_string", mongoURL)
                .put("db_name", mongoDBName);
        mongoClient = createNonSharedMongoDB(vertx,mongoconfig);
        startFetchEventBusConsumer();

    }

/*
this method will listen on fetch.link channel and once new event
received it will fetch the urls' content and persist in db,
in case of any failure the link saved in a kafka topic.
 */
    private void startFetchEventBusConsumer() {
        vertx.eventBus().localConsumer("fetch.link", message -> {
            String urlLink = (String) message.body();
            if(urlValidator(urlLink))
            {
                try {
                    log.info("Extracting Content");
                    String content = webPageContentFetcherService.getWebPageContent(urlLink);
                    log.info("Inserting fetched Document to DB. " + urlLink);
                    /*
                    inserting content in URLContent document, assuming url hash as key to avoid duplication.
                     */
                    JsonObject document = new JsonObject()
                            .put("urlLink", urlLink)
                            .put("content", content)
                            .put("_id", urlLink.hashCode());;
                    mongoClient.save("URLContent",document,res -> {
                        if (res.succeeded()) {
                            String id = res.result();
                            log.info("Saved book with id " + id);
                        } else {
                            log.warn("Couldn't extract data. insert url in unprocessed topic. " + urlLink);
                            vertx.eventBus().send("unprocessed.link", urlLink);
                        }
                    });
                } catch (IOException e) {
                    log.warn("something went wrong. insert url in unprocessed topic. " + urlLink);
                    vertx.eventBus().send("unprocessed.link", urlLink);
                }
            }
            else
            {
                log.warn("Invalid URL. insert url in invalid topic. " + urlLink);
                vertx.eventBus().send("invalid.link", urlLink);
            }

        });
    }


    public  boolean urlValidator(String url)
    {
        try {
            new URL(url).toURI();
            return true;
        }
        catch (URISyntaxException exception) {
            return false;
        }
        catch (MalformedURLException exception) {
            return false;
        }
    }
}
