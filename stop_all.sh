#!/bin/bash

echo "Stopping Tomcat"
unset CATALINA_HOME
unset CATALINA_BASE
cd freeeed-tomcat/bin;
./shutdown.sh &

cd ../..


# Kill Solr
echo "Stopping Solr..."
pkill -f "java.*start.jar"

# Kill Tika
echo "Stopping Tika..."
pkill -f "java.*tika-server.jar"

# Kill FreeEed UI
echo "Stopping FreeEed UI..."
pkill -f "java.*FreeEedUI"

# Kill Python backend
echo "Stopping Python backend..."
pkill -f "python.*uvicorn"

# Kill AI Advisor (standalone executable)
echo "Stopping AI Advisor..."
pkill -f "AiAdvisor" 2>/dev/null || true

echo "All services stopped."
