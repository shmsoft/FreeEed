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


if command -v flock > /dev/null; then
    exec 9>/tmp/tika.lock || exit 1
    flock -n 9 || {
      echo "Tika already started"
      exit 0
    }
else
    echo "flock not found, skipping lock check..."
fi

echo "Starting Tika..."
cd freeeed-tika || exit 1
nohup java -Xmx1024M -jar tika-server.jar > ../logs/tika.log 2>&1 &
cd ..

cd FreeEed
chmod +x freeeed_player.sh
./freeeed_player.sh &

# Always run from this script's directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ -d "../python" ]; then
    echo "Starting Python backend..."
    cd ../python
    if [ -f "myenv/bin/activate" ]; then
        source myenv/bin/activate
        exec python -m uvicorn main:app --reload
    else
        echo "Warning: Python virtual environment not found at ../python/myenv"
    fi
else
    echo "Warning: Python directory ../python not found. Python backend will not start."
fi
