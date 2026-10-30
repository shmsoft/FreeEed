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

# Review admin password for THIS appliance, generated on FIRST BOOT.
#
# Deliberately not generated during provisioning: the image is built once and
# downloaded many times, so a provision-time password would be the SAME on every
# customer's appliance -- which is the defect FreeEedUI #68 removed in the first
# place. Generating it here means each VM gets its own on first start.
#
# FREEEED_ADMIN_PASSWORD is read by the review app when it creates the admin
# account; it only has an effect on the very first start, and the credentials
# file is what marks that as done.
CREDENTIALS_FILE=/opt/freeeed/review-admin-credentials.txt
if [ ! -f "$CREDENTIALS_FILE" ]; then
    # No O/0/I/l/1 -- this gets read off a console and retyped.
    generated="$(LC_ALL=C tr -dc 'A-HJ-NP-Za-km-z2-9' < /dev/urandom 2>/dev/null | head -c 16)"
    if [ ${#generated} -eq 16 ]; then
        export FREEEED_ADMIN_PASSWORD="$generated"
        ip="$(hostname -I 2>/dev/null | awk '{print $1}')"
        [ -z "$ip" ] && ip="<appliance-ip>"

        (
            umask 077
            cat > "$CREDENTIALS_FILE" <<CREDS
FreeEed Review -- administrator account for THIS appliance

    url:        http://$ip:8090/freeeedui
    username:   admin
    password:   $generated

Generated on first boot; every appliance gets its own. Change it after signing
in, then delete this file.
CREDS
        )
        chmod 600 "$CREDENTIALS_FILE" 2>/dev/null || true
        echo "Review admin password generated for this appliance: $CREDENTIALS_FILE"
    else
        echo "WARNING: could not generate a review admin password; the review app" >&2
        echo "will generate its own and log where it wrote it." >&2
    fi
fi

./start_dev_services.sh
echo "FreeEed appliance up: Tomcat/FreeEedUI (:8090) + Solr (:8983) + Tika (:9998)"
if [ -f "$CREDENTIALS_FILE" ]; then
    echo "Review sign-in credentials: $CREDENTIALS_FILE"
fi
