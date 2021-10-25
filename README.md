# NAK-MCI
1. create a folder in your local machin e.g 'source'
2. create TimeStampFile.cfg file in "source" directory and add a line with below format
    LAST_PROCESSED_TIME=2021-10-22 23:42:00.211
3. create a folder with name 'urls' in source directory and add files which contains web page urls
4. update valume for feader service in docker compose file with created directory in step 1
5. select source_type in feeder service (allowed values are Kafka and File, default is Kafka).
6. select webpage_fetchMethod, (allowed values are URL and WebElement, default value is WebElement)
7. install feeder module, it will create a docker image with name:tag feeder:1.0.0-SNAPSHOT
8. install fetcher module, it will create a docker image with name:tag fetcher:1.0.0-SNAPSHOT
9. run docker-compose
