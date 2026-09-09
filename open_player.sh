#!/bin/bash
# Launch the FreeEed Player (the processing GUI), fully detached from the caller.
#
# This is invoked from the Control Panel via ProcessBuilder, which does NOT drain
# the child's stdout/stderr. A plain `./freeeed_player.sh &` leaves the player
# writing to a pipe whose reader goes away when this script exits, so the player
# dies (SIGPIPE) or blocks with no window -- it "launches" but never appears.
# setsid + nohup put it in its own session and redirect its output to a log, so
# it survives independently of how it was started.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Don't start a second player -- the local DB/UI is single-instance, and the
# Control Panel now auto-launches the player on "Start All Services", so a stray
# duplicate would otherwise pile up. If one is already up, leave it.
if pgrep -f "java.*org.freeeed.ui.FreeEedUI" >/dev/null 2>&1; then
    echo "FreeEed Player already running."
    exit 0
fi

echo "Starting FreeEed Player..."
cd "$SCRIPT_DIR/FreeEed" || { echo "ERROR: FreeEed/ not found under $SCRIPT_DIR" >&2; exit 1; }

# Detach so the player survives this launcher exiting (see header). setsid is
# Linux-only (util-linux); stock macOS has no setsid, so fall back to nohup there
# -- nohup + output redirect alone already fixes the Control Panel case (no
# controlling terminal to lose). Either way, redirect to a log so the player never
# writes to an inherited, undrained pipe.
if command -v setsid >/dev/null 2>&1; then
    setsid nohup ./freeeed_player.sh >"$SCRIPT_DIR/player.log" 2>&1 &
else
    nohup ./freeeed_player.sh >"$SCRIPT_DIR/player.log" 2>&1 &
fi
