package com.nak.mci.consumer;

import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Slf4j
@Service
public class FileConsumerService  implements SourceConsumer{
    @Value("${source.file.path}")
    private String path;
    @Value("${source.file.timeStampFilePath}")
    private String timeStampFilePath;
    @Value("${source.file.pollingInterval}")
    private int pollingInterval;

    @Value("${destination.kafka.url}")
    private String kafkaUrl;

    @Value("${destination.kafka.destination-topic}")
    private String kafkaTopic;


    KafkaProducer kafkaProducer;
    Vertx vertx;


    @Override
    public void runConsumer(Vertx vertx) throws InterruptedException {
        this.vertx = vertx;
        kafkaProducer = createKafkaProducer();
        /*
        set a scheduleAtFixedRate to periodically check the working directory for new files
         */
        Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                consumeNewFiles();
            };
        };
        t.scheduleAtFixedRate(tt,500,pollingInterval*1000);


    }


    /**
     * this method check working directory and filter unprocessed
     * files (files with modified time after LastProcessedTimeStamp which saved in timeStampFilePath  )
     * and call streamFileToKafka to process new files.
     * finally newest modified time of filtered files saved in a timeStampFilePath file
     *
     */
    private void consumeNewFiles()
    {
            Date tempLastProcessedTimeStamp = getTimestamp(timeStampFilePath);
            System.out.println(tempLastProcessedTimeStamp);
            List<File> fileToProcess = Arrays.stream((new File(path)).listFiles()).filter(file -> (new Date(file.lastModified()).after(getTimestamp(timeStampFilePath)))).collect(Collectors.toList());
            for(File file : fileToProcess)
            {
                try {
                    streamFileToKafka(file.getAbsolutePath());
                    if((new Date(file.lastModified())).after(tempLastProcessedTimeStamp))
                        tempLastProcessedTimeStamp = (new Date(file.lastModified()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            log.info("Going to Update Time");
            if(tempLastProcessedTimeStamp.after(getTimestamp(timeStampFilePath)))
                updateTimeStampFile(tempLastProcessedTimeStamp);
            log.info("All Done I am Sleeping Now for "
                    + pollingInterval + "  Minutes...");


    }


    /**
     * read fileName line by line and send each line to destination kafka topic
     * @param fileName
     * @throws IOException
     */

    public void streamFileToKafka(String fileName) throws IOException {
        //KafkaProducerRecord<String, String> record ;
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(s -> {
                        KafkaProducerRecord<String, String> record = KafkaProducerRecord.create(kafkaTopic, s);
                        kafkaProducer.write(record);
                    }
                    );
        }
    }


    private void updateTimeStampFile(Date time) {
        // This method will contain logic for updating the time stamp
        try {
            log.info("New Time is : " + time);
            ArrayList<String> lines = new ArrayList<String>();
            String line = null;

            FileReader fr = new FileReader(timeStampFilePath);
            BufferedReader br = new BufferedReader(fr);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            while ((line = br.readLine()) != null) {
                if (line.contains("LAST_PROCESSED_TIME"))
                    line = "LAST_PROCESSED_TIME=" + df.format(time);
                lines.add(line);
            }
            fr.close();
            br.close();
            FileWriter fw = new FileWriter(timeStampFilePath);
            BufferedWriter out = new BufferedWriter(fw);
            for (String s : lines) {
                out.write(s + "\n");
            }
            out.flush();
            fw.close();
            out.close();
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace());
        }
    }

    private Date getTimestamp(String timeStampFilePath) {
        BufferedReader br = null;
        String dateToReturn = "";
        Date tDate = new Date();
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(timeStampFilePath));
            while ((sCurrentLine = br.readLine()) != null) {
                dateToReturn = sCurrentLine.split("=")[1];
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            tDate = df.parse(dateToReturn.trim());

        } catch (Exception e) {
            log.error("Couldn't process file "+e.getMessage());
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                System.out.println(ex.getStackTrace());
            }
        }
        return tDate;
    }



    public KafkaProducer createKafkaProducer() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", kafkaUrl);
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("acks", "1");

        // use producer for interacting with Apache Kafka
        KafkaProducer<String, String> producer = KafkaProducer.create(vertx, config);
        return producer;
    }


}
