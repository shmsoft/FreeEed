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

# Make Tomcat's launchers executable, and nothing more.
#
# This used to be `chmod -R 755 freeeed-tomcat`, which also re-opened the review
# app's credential files on EVERY start: work/initial-admin-password.txt (the
# generated admin password, in clear) and work/u.dat (every account's password
# hash). FSUserDao deliberately creates those 600; the blanket chmod undid that
# from the outside, so each half looked correct and only the combination was
# wrong (#633). A first start hid it -- the chmod runs before Tomcat creates the
# file -- and the SECOND start exposed it.
chmod +x freeeed-tomcat/bin/*.sh 2>/dev/null
# Directories must stay traversable by the user running Tomcat. Owner bits only:
# this deliberately does not touch file modes or grant anything to other users.
find freeeed-tomcat -type d -exec chmod u+rwx {} + 2>/dev/null
# Repair installs already exposed by the old blanket chmod. Upgrading users have
# world-readable password hashes on disk right now; fixing the script alone
# would leave them that way.
find freeeed-tomcat -type f \( -name 'initial-admin-password.txt' -o -name 'u.dat' \
     -o -name 'c.dat' \) -exec chmod 600 {} + 2>/dev/null

# Ensure logs directory exists
mkdir -p logs

# Tomcat sets java.io.tmpdir to $CATALINA_BASE/temp; if that dir is missing,
# anything creating a temp file (PDF export/merge, uploads, JSP) dies with
# "No such file or directory". Guarantee it exists on every start.
mkdir -p freeeed-tomcat/temp

cd freeeed-tomcat/bin;
./startup.sh &
cd ../..

# Is a TCP port already being listened on? Used to avoid starting a second
# service that would only fail to bind. Returns non-zero when it cannot tell,
# so a machine with none of these tools behaves exactly as before.
port_in_use() {
    if command -v ss > /dev/null 2>&1; then
        ss -ltn 2>/dev/null | grep -q ":$1[[:space:]]"
    elif command -v lsof > /dev/null 2>&1; then
        lsof -iTCP:"$1" -sTCP:LISTEN > /dev/null 2>&1
    elif command -v netstat > /dev/null 2>&1; then
        netstat -ltn 2>/dev/null | grep -q ":$1[[:space:]]"
    else
        return 1
    fi
}

# Solr had no guard at all. This started a second Solr unconditionally; it then
# failed to bind 8983 and shut itself down, leaving the stack quietly served by
# whatever Solr was already running -- possibly from an older install. During
# release testing that produced three green status dots over a two-month-old
# Solr and Tika under a brand-new Tomcat and war: nothing appeared broken, and
# every result taken from it was meaningless (#634). Say so instead.
if port_in_use 8983; then
    echo "Solr: port 8983 is already in use -- NOT starting another one."
    echo "      The existing process keeps serving. If it is left over from an"
    echo "      earlier run or an older install, stop all services first, or"
    echo "      results will come from a service you did not just start."
else
    echo "Starting Solr..."
    cd freeeed-solr/example
    nohup "$JAVA_CMD" -Xmx1024M -jar start.jar > ../../logs/solr.log 2>&1 &
    cd ../..
fi


# Tika's lock is held by an fd the server process inherits, so it stays held for
# as long as Tika runs -- which makes it a liveness check, not just a guard
# against two simultaneous starts.
#
# An already-running Tika is a reason to SKIP STARTING TIKA. It is not a reason
# to abandon the rest of this script, which is what `exit 0` did here: services
# came up, the Control Panel reported "Services running - Player opening...",
# and the Player launch eighteen lines below never ran. No error, no entry in
# player.log, nothing on screen to suggest anything had gone wrong (#634).
START_TIKA=true
if command -v flock > /dev/null; then
        LOCK_BASE_DIR="${XDG_RUNTIME_DIR:-${TMPDIR:-$SCRIPT_DIR/logs}}"
        mkdir -p "$LOCK_BASE_DIR" || exit 1
        LOCK_FILE="$LOCK_BASE_DIR/tika.lock"
        exec 9>"$LOCK_FILE" || exit 1
    if ! flock -n 9; then
        echo "Tika is already running -- leaving it alone."
        START_TIKA=false
    fi
else
    echo "flock not found, skipping lock check..."
fi

if [ "$START_TIKA" = true ]; then
    echo "Starting Tika..."
    cd freeeed-tika || exit 1
    nohup "$JAVA_CMD" -Xmx1024M -jar tika-server.jar > ../logs/tika.log 2>&1 &
    cd ..
fi

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
