#!/bin/bash
# this script should be run from freeeed_complete_pack
echo "******************** Starting FreeEed development services"

# Always run from this script's directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# Resolve a working Java runtime up front. Without this the bare `java` calls
# below hit macOS's /usr/bin/java stub on a machine with no JDK and every service
# dies silently (nohup discards the exit code) while this script reports success.
. "$SCRIPT_DIR/find_java.sh"
freeeed_require_java || exit 1

unset CATALINA_HOME
unset CATALINA_BASE

chmod -R 755 freeeed-tomcat

# Ensure logs directory exists
mkdir -p logs

# Tomcat sets java.io.tmpdir to $CATALINA_BASE/temp; if that dir is missing,
# anything creating a temp file (PDF export/merge, uploads, JSP) dies with
# "No such file or directory". Guarantee it exists on every start.
mkdir -p freeeed-tomcat/temp

cd freeeed-tomcat/bin;
./startup.sh &
cd ../..

echo "Starting Solr..."
cd freeeed-solr/example
nohup "$JAVA_CMD" -Xmx1024M -jar start.jar > ../../logs/solr.log 2>&1 &
cd ../..


if command -v flock > /dev/null; then
        LOCK_BASE_DIR="${XDG_RUNTIME_DIR:-${TMPDIR:-$SCRIPT_DIR/logs}}"
        mkdir -p "$LOCK_BASE_DIR" || exit 1
        LOCK_FILE="$LOCK_BASE_DIR/tika.lock"
        exec 9>"$LOCK_FILE" || exit 1
    flock -n 9 || {
      echo "Tika already started"
      exit 0
    }
else
    echo "flock not found, skipping lock check..."
fi

echo "Starting Tika..."
cd freeeed-tika || exit 1
nohup "$JAVA_CMD" -Xmx1024M -jar tika-server.jar > ../logs/tika.log 2>&1 &
cd ..

# Start the Player through open_player.sh, the one launch path: it detaches the
# Player and logs to player.log, and won't start a second one. A bare
# `./freeeed_player.sh &` here left the Player writing into whatever pipe started
# this script (the Control Panel never reads it) and raced the Control Panel's own
# open_player call.
cd "$SCRIPT_DIR" || exit 1
chmod +x FreeEed/freeeed_player.sh open_player.sh
./open_player.sh

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
