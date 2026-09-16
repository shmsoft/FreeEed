#!/bin/bash
# Start the FreeEed appliance services headlessly: Tomcat(FreeEedUI) + Solr + Tika.
# Deliberately does NOT start the Swing Player (this is a server, browser-accessed).
#
# NOTE: the pack's start_dev_services.sh already starts ALL THREE (Tomcat, Solr, Tika).
# Do NOT also call freeeed-tomcat/bin/startup.sh here -- that double-starts Tomcat, the
# second instance fails to bind 8090/8009/8005, and 8090 ends up served by neither
# (verified on the first appliance build, 2026-09-16).
set -uo pipefail
cd /opt/freeeed
if [ -f ./find_java.sh ]; then . ./find_java.sh; freeeed_require_java || exit 1; fi
./start_dev_services.sh
echo "FreeEed appliance up: Tomcat/FreeEedUI (:8090) + Solr (:8983) + Tika (:9998)"
