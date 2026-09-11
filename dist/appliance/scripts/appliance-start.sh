#!/bin/bash
# Start the FreeEed appliance services headlessly: Solr + Tika + Tomcat(FreeEedUI).
# Deliberately does NOT start the Swing Player (this is a server, browser-accessed).
# SCAFFOLD 2026-09-11 -- confirm the exact pack script/paths on the first build.
set -euo pipefail
cd /opt/freeeed

# Resolve a JRE and export JAVA_HOME for Tomcat's catalina.sh (the pack ships find_java.sh).
if [ -f ./find_java.sh ]; then . ./find_java.sh; freeeed_require_java || exit 1; fi

# Solr + Tika (the pack's dev-services script starts both, headless).
./start_dev_services.sh

# Tomcat + FreeEedUI on :8090.
if [ -x ./freeeed-tomcat/bin/startup.sh ]; then
  JAVA_HOME="${JAVA_HOME:-}" ./freeeed-tomcat/bin/startup.sh
else
  echo "ERROR: freeeed-tomcat/bin/startup.sh not found -- confirm pack layout" >&2
  exit 1
fi

echo "FreeEed appliance up: Solr + Tika + Tomcat/FreeEedUI (http://<ip>:8090/freeeedui)"
