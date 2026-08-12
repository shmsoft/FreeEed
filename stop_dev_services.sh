#!/bin/bash
#
# Stop the dev services started by start_dev_services.sh (Tomcat, Solr, Tika).
# The counterpart to start_dev_services.sh - run it after a build/install/test
# cycle to leave the machine clean.
#
# Kills Tomcat reliably: graceful shutdown first, then a force-kill fallback,
# because a hung Tomcat keeps the shutdown port and strands :8090, colliding
# with the next start.
#
echo "Stopping Tomcat..."
unset CATALINA_HOME
unset CATALINA_BASE
if [ -x freeeed-tomcat/bin/shutdown.sh ]; then
    ( cd freeeed-tomcat/bin && ./shutdown.sh 2>/dev/null ) || true
fi
pkill -f "org.apache.catalina.startup.Bootstrap" 2>/dev/null || true

echo "Stopping Solr..."
pkill -f "java.*start.jar" 2>/dev/null || true

echo "Stopping Tika..."
pkill -f "java.*tika-server.jar" 2>/dev/null || true

echo "Dev services stopped."
