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
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

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

# Start the FastAPI application with Uvicorn
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]
