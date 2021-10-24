FROM openjdk:latest
COPY target/feederApp-1.0.0-SNAPSHOT-exec.jar app.jar
RUN mkdir -p source/urls
ENTRYPOINT ["java","-jar","/app.jar"]