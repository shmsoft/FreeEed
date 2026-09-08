#!/bin/bash
# Launch the FreeEed Player (the processing GUI), fully detached from the caller.
#
# This is invoked from the Control Panel via ProcessBuilder, which does NOT drain
# the child's stdout/stderr. A plain `./freeeed_player.sh &` leaves the player
# writing to a pipe whose reader goes away when this script exits, so the player
# dies (SIGPIPE) or blocks with no window -- it "launches" but never appears.
# nohup + a full redirect fix that. Detaching into a new session additionally
# stops the player dying with whatever started it.
#
# PORTABILITY: setsid is util-linux and DOES NOT EXIST ON macOS -- `setsid ...`
# there fails with "command not found" and the player never launches at all. So
# use setsid when present (Linux) and otherwise fall back to launching from a
# subshell, which reparents the child to PID 1 on exit and achieves the same
# detachment. Verified on macOS: the child survives its parent and keeps running.

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
if command -v setsid >/dev/null 2>&1; then
    setsid nohup ./freeeed_player.sh >"$SCRIPT_DIR/player.log" 2>&1 &
else
    # macOS: no setsid. The subshell exits immediately, so the player is
    # reparented to PID 1 -- detached, with its output still redirected.
    ( nohup ./freeeed_player.sh >"$SCRIPT_DIR/player.log" 2>&1 & )
fi
