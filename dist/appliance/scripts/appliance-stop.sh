#!/bin/bash
# Stop the FreeEed appliance services. The pack's stop_dev_services.sh stops all three
# (Tomcat, Solr, Tika), so just call it -- mirrors appliance-start.sh.
set -uo pipefail
cd /opt/freeeed
[ -x ./stop_dev_services.sh ] && ./stop_dev_services.sh || pkill -9 java 2>/dev/null || true
echo "FreeEed appliance stopped."
