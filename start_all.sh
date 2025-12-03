#!/bin/bash
# this script should be run from freeeed_complete_pack
echo "******************** Starting FreeEed development services"

unset CATALINA_HOME
unset CATALINA_BASE

chmod -R 755 freeeed-tomcat

# Ensure logs directory exists
mkdir -p logs

cd freeeed-tomcat/bin;
./startup.sh &
cd ../..

echo "Starting Solr..."
cd freeeed-solr/example
nohup java -Xmx1024M -jar start.jar > ../../logs/solr.log 2>&1 &
cd ../..

echo "Starting Tika..."
cd freeeed-tika
nohup java -Xmx1024M -jar tika-server.jar > ../logs/tika.log 2>&1 &
cd ..

cd FreeEed
./freeeed_player.sh &

#!/usr/bin/env bash
set -e

# Always run from this script's directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

source myenv/bin/activate
exec python -m uvicorn main:app --reload
