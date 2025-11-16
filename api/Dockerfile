FROM maven:3.8-openjdk-11 as builder

WORKDIR /app

# Copy source files
COPY . .

# Build the application
RUN cd freeeed-processing && \
    mvn clean package -DskipTests

FROM python:3.11-slim

WORKDIR /app

# Install system dependencies including build tools
RUN apt-get update && \
    apt-get install -y \
    default-jdk \
    curl \
    build-essential \
    g++ \
    sqlite3 \
    libsqlite3-dev \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Create SQLite database directory
RUN mkdir -p /app/db && \
    touch /app/db/freeeed.db && \
    chmod 777 /app/db/freeeed.db

# Copy the FreeEed JAR from builder
COPY --from=builder /app/freeeed-processing/target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar /app/freeeed-processing.jar

# Install Python dependencies
COPY api/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy FastAPI application
COPY api/ .

# Create data directories
RUN mkdir -p /data/input /data/output

# Set environment variables
ENV PYTHONUNBUFFERED=1
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

# Expose port
EXPOSE 8000

# Add debugging information
RUN echo "Content of /app:" && ls -la /app && \
    echo "\nContent of /app/test-data:" && ls -la /app/test-data || true && \
    echo "\nJava version:" && java -version

# Start the FastAPI application with Uvicorn
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000", "--reload", "--log-level", "debug"]
