#!/bin/bash
# this script should be run from freeeed_complete_pack
echo "******************** Starting FreeEed development services"

unset CATALINA_HOME
unset CATALINA_BASE

chmod -R 755 freeeed-tomcat

# Ensure logs directory exists
mkdir -p logs

echo "Starting Solr..."
cd freeeed-solr/example
nohup java -Xmx1024M -jar start.jar > ../../logs/solr.log 2>&1 &
cd ../..

echo "Starting Tika..."
cd freeeed-tika
nohup java -Xmx1024M -jar tika-server.jar > ../logs/tika.log 2>&1 &
cd ..

echo "All services started. Logs are in ./logs/"

