#!/bin/bash
# Stop the FreeEed appliance services (Tomcat, then Solr + Tika).
# SCAFFOLD 2026-09-11 -- confirm pack script names on the first build.
set -uo pipefail
cd /opt/freeeed
if [ -f ./find_java.sh ]; then . ./find_java.sh; freeeed_require_java || true; fi
[ -x ./freeeed-tomcat/bin/shutdown.sh ] && ./freeeed-tomcat/bin/shutdown.sh || true
[ -x ./stop_dev_services.sh ] && ./stop_dev_services.sh || true
echo "FreeEed appliance stopped."
