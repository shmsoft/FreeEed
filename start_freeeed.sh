#!/usr/bin/env bash
set -e

echo "******************** Starting FreeEed services"

# Always run from this script's directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

unset CATALINA_HOME
unset CATALINA_BASE

mkdir -p logs

# ---------------- Tomcat ----------------
echo "Starting Tomcat..."
cd freeeed-tomcat/bin
./startup.sh
cd ../..

# ---------------- Solr ----------------
echo "Starting Solr..."
cd freeeed-solr/example
nohup java -Xmx1024M -jar start.jar > ../../logs/solr.log 2>&1 &
echo $! > ../../logs/solr.pid
cd ../..

# ---------------- Tika ----------------
echo "Starting Tika..."
cd freeeed-tika
nohup java -Xmx1024M -jar tika-server.jar > ../logs/tika.log 2>&1 &
echo $! > ../logs/tika.pid
cd ..

# ---------------- Python (Uvicorn) ----------------
# Try common locations for the Python service
PYTHON_DIR=""

if [[ -d "$SCRIPT_DIR/FreeEed/python" ]]; then
  PYTHON_DIR="$SCRIPT_DIR/FreeEed/python"
elif [[ -d "$SCRIPT_DIR/python" ]]; then
  PYTHON_DIR="$SCRIPT_DIR/python"
fi

if [[ -z "$PYTHON_DIR" ]]; then
  echo "WARNING: Could not find Python service directory (tried FreeEed/python and python). Python services will NOT start." >&2
else
  echo "Starting Python (Uvicorn) in $PYTHON_DIR..."
  (
    cd "$PYTHON_DIR"

    if [[ ! -f "myenv/bin/activate" ]]; then
      echo "WARNING: virtualenv myenv not found in $PYTHON_DIR; Python services will NOT start." >&2
      exit 0
    fi

    # Activate venv and start Uvicorn
    source myenv/bin/activate
    exec python -m uvicorn main:app --reload
  ) > "$SCRIPT_DIR/logs/python.log" 2>&1 &
  echo $! > "$SCRIPT_DIR/logs/python.pid"
fi

# ---------------- FreeEed Player (Swing) ----------------
echo "Starting FreeEed Player (Swing)..."
cd FreeEed
./freeeed_player.sh &
echo $! > ../logs/player.pid
cd ..

echo "✅ All FreeEed services started"

