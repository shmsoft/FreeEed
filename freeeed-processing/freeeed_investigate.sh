#!/bin/bash
# Resolve a working Java runtime. In the pack this runs from FreeEed/, so the
# shared resolver is one level up; from a source checkout it may be absent, in
# which case fall back to whatever `java` is on PATH.
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
if [ -f "$SCRIPT_DIR/../find_java.sh" ]; then
    . "$SCRIPT_DIR/../find_java.sh"
    freeeed_require_java || exit 1
else
    JAVA_CMD="${JAVA_CMD:-java}"
fi

"$JAVA_CMD" -Xms512m -Xmx1024m \
-cp target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar \
org.freeeed.piranha.Investigate $1
