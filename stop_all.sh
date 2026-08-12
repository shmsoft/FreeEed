#!/bin/bash

echo "Stopping Tomcat"
unset CATALINA_HOME
unset CATALINA_BASE
if [ -x freeeed-tomcat/bin/shutdown.sh ]; then
    ( cd freeeed-tomcat/bin && ./shutdown.sh 2>/dev/null ) || true
fi
# Force-kill any Tomcat that didn't stop gracefully. A hung instance keeps the
# shutdown port and strands :8090, colliding with the next start.
pkill -f "org.apache.catalina.startup.Bootstrap" 2>/dev/null || true


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
