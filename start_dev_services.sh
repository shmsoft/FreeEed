#!/bin/bash
# this script should be run from freeeed_complete_pack
echo "******************** this script should be run from freeeed_complete_pack"

# Resolve this script's directory before any cd, then resolve Java. Without this
# the bare `java` calls below hit macOS's /usr/bin/java stub on a machine with no
# JDK: Solr and Tika die instantly while the script reports success.
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
. "$SCRIPT_DIR/find_java.sh"
freeeed_require_java || exit 1

#
echo off

unset CATALINA_HOME
unset CATALINA_BASE
chmod -R 755 freeeed-tomcat
# fix for Mac
chmod u+x freeeed-tomcat/bin/*.sh

# ---------------- Tomcat (serves the review web app at /freeeedui) ----------------
echo "Starting Tomcat..."
cd freeeed-tomcat/bin
./startup.sh
cd ../..

cd freeeed-solr/example
"$JAVA_CMD" -Xmx1024M -jar start.jar &
cd ../..

cd freeeed-tika
"$JAVA_CMD" -Xmx1024M -jar tika-server.jar &
cd ..

