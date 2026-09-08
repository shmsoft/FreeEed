#!/bin/bash
# find_java.sh — locate a WORKING Java runtime, or fail with a usable message.
#
# Sourced by the pack's launch scripts. Sets:
#   JAVA_CMD   absolute path to a working `java`
#   JAVA_HOME  exported (Tomcat's catalina.sh requires JAVA_HOME or JRE_HOME)
#
# Why this exists: the pack has no bundled JRE and the scripts used to call bare
# `java`. On macOS /usr/bin/java is ALWAYS present -- it is a stub that prints
# "Unable to locate a Java Runtime" when no JDK is installed. So testing for the
# binary is not enough; we must run it. Worse, the services were started with
# `nohup ... &`, discarding the exit code, so on a Mac with no JDK every service
# died instantly while the Control Panel reported success. A GUI (Finder) launch
# gets PATH=/usr/bin:/bin:/usr/sbin:/sbin and no JAVA_HOME, so this is the normal
# user path, not an edge case.

# Accept a candidate only if it actually runs.
_freeeed_try_java() {
    [ -n "$1" ] && [ -x "$1" ] || return 1
    "$1" -version >/dev/null 2>&1 || return 1
    JAVA_CMD="$1"
    JAVA_HOME="$(cd "$(dirname "$1")/.." && pwd)"
    export JAVA_HOME
    return 0
}

freeeed_find_java() {
    _here="$(cd "$(dirname "${BASH_SOURCE[0]:-$0}")" && pwd)"

    # 1. A JRE bundled in the pack (future jpackage/jlink layout) wins.
    _freeeed_try_java "$_here/runtime/bin/java" && return 0
    _freeeed_try_java "$_here/jre/bin/java" && return 0

    # 2. An explicitly configured JAVA_HOME.
    [ -n "${JAVA_HOME:-}" ] && _freeeed_try_java "$JAVA_HOME/bin/java" && return 0

    # 3. macOS's registry of installed JDKs.
    if [ -x /usr/libexec/java_home ]; then
        _jh="$(/usr/libexec/java_home 2>/dev/null)"
        [ -n "$_jh" ] && _freeeed_try_java "$_jh/bin/java" && return 0
    fi

    # 4. Whatever is on PATH (the macOS stub fails the -version check above).
    _onpath="$(command -v java 2>/dev/null)"
    _freeeed_try_java "$_onpath" && return 0

    # 5. Common install locations, including hand-extracted tarballs.
    for _d in \
        /Library/Java/JavaVirtualMachines/*/Contents/Home \
        "$HOME"/Library/Java/JavaVirtualMachines/*/Contents/Home \
        "$HOME"/tools/*/Contents/Home \
        "$HOME"/tools/* \
        /usr/lib/jvm/* \
        /opt/homebrew/opt/openjdk*/libexec/openjdk.jdk/Contents/Home \
        /usr/local/opt/openjdk*/libexec/openjdk.jdk/Contents/Home
    do
        _freeeed_try_java "$_d/bin/java" && return 0
    done

    return 1
}

# Resolve or abort. Callers should use "$JAVA_CMD", never bare `java`.
freeeed_require_java() {
    if freeeed_find_java; then
        echo "Using Java: $JAVA_CMD"
        return 0
    fi
    cat >&2 <<'MSG'

======================================================================
ERROR: FreeEed could not find a Java runtime.

FreeEed needs Java 11 or newer to run. Nothing has been started.

  macOS:  install Temurin from https://adoptium.net  (or: brew install --cask temurin)
  Linux:  sudo apt install openjdk-17-jre     (Debian/Ubuntu)
          sudo dnf install java-17-openjdk    (Fedora/RHEL)

If Java IS installed but not being found, point FreeEed at it directly:

  export JAVA_HOME=/path/to/jdk
  then start FreeEed again.

Note: on macOS `java -version` may print "Unable to locate a Java Runtime"
even though /usr/bin/java exists -- that file is a stub, not a real runtime.
======================================================================

MSG
    return 1
}
