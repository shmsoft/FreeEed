FROM maven:3.8-openjdk-11 as builder

WORKDIR /app

# Copy source files
COPY . .

# Build the application
RUN cd freeeed-processing && \
    mvn clean package -DskipTests

FROM openjdk:11-jre-slim

WORKDIR /app
COPY --from=builder /app/freeeed-processing/target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar ./freeeed-processing.jar

ENV JAVA_OPTS="-Xmx2048m"

CMD ["java", "-jar", "freeeed-processing.jar"]
