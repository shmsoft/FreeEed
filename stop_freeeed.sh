#!/usr/bin/env bash
set -e

echo "******************** Stopping FreeEed services"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

LOG_DIR="logs"

stop_by_pid() {
  NAME="$1"
  PID_FILE="$2"

  if [[ -f "$PID_FILE" ]]; then
    PID=$(cat "$PID_FILE")

    if kill -0 "$PID" 2>/dev/null; then
      echo "Stopping $NAME (PID $PID)..."
      kill "$PID"
      sleep 2

      if kill -0 "$PID" 2>/dev/null; then
        echo "$NAME did not stop gracefully, forcing..."
        kill -9 "$PID"
      fi
    else
      echo "$NAME not running (stale PID)"
    fi

    rm -f "$PID_FILE"
  else
    echo "No PID file for $NAME"
  fi
}

# ---------------- Python ----------------
stop_by_pid "Python (Uvicorn)" "$LOG_DIR/python.pid"

# ---------------- FreeEed Player ----------------
stop_by_pid "FreeEed Player (Swing)" "$LOG_DIR/player.pid"

# ---------------- Tika ----------------
stop_by_pid "Tika" "$LOG_DIR/tika.pid"

# ---------------- Solr ----------------
stop_by_pid "Solr" "$LOG_DIR/solr.pid"

# ---------------- Tomcat ----------------
echo "Stopping Tomcat..."
if [[ -x freeeed-tomcat/bin/shutdown.sh ]]; then
  freeeed-tomcat/bin/shutdown.sh
else
  echo "Tomcat shutdown script not found"
fi

echo "✅ FreeEed services stopped"

