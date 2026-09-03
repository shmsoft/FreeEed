#!/bin/bash
#
# Wrapper around release_freeeed_complete.sh — the entry point you actually run.
#
# DEFAULT = development build for the machine you're sitting at: builds the pack
# plus THIS platform's installer only, and NEVER uploads to S3.
#   - on macOS  -> pack + .dmg          (no .run, no .exe, no S3)
#   - on Linux  -> pack + .run          (no .dmg, no .exe, no S3)
#
# Publishing is a separate, deliberate act -- never a side effect of a development
# build -- and is refused unless the FreeEed repo is on `dev`.
#
#   ./release.sh                 # dev build: this platform only, NO S3 (default)
#   PUBLISH=1 ./release.sh       # real release: uploads to S3 (dev branch only)
#   RELEASE_DRY_RUN=1 ./release.sh   # print the resolved settings and exit
#
# This lives in the repo so the two machines can't drift (see
# freeeed-processing/docs/working-cadence.md). It is normally symlinked as
# $SHMSOFT_HOME/release/release.sh, so it resolves its own real location below
# rather than assuming a working directory.
#
set -euo pipefail

# Resolve this script's real path, following symlinks (portable: macOS's readlink
# has no -f on older releases).
SOURCE="${BASH_SOURCE[0]}"
while [ -L "$SOURCE" ]; do
    LINKDIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
    SOURCE="$(readlink "$SOURCE")"
    [ "${SOURCE#/}" = "$SOURCE" ] && SOURCE="$LINKDIR/$SOURCE"
done
FREEEED_REPO="$(cd -P "$(dirname "$SOURCE")" && pwd)"
REAL_RELEASE="$FREEEED_REPO/release_freeeed_complete.sh"

# The repo's parent holds FreeEed/ and FreeEedUI/ -- that's SHMSOFT_HOME.
: "${SHMSOFT_HOME:=$(dirname "$FREEEED_REPO")}"
export SHMSOFT_HOME

# ai_advisor / backup_restore payload lives beside it in scaia/. Only defaulted
# when that directory actually exists; the release warns and skips if absent.
if [ -z "${SCAIA_HOME:-}" ] && [ -d "$(dirname "$SHMSOFT_HOME")/scaia" ]; then
    SCAIA_HOME="$(dirname "$SHMSOFT_HOME")/scaia"
    export SCAIA_HOME
fi

if [ ! -x "$REAL_RELEASE" ]; then
    echo "ERROR: cannot find $REAL_RELEASE" >&2
    exit 1
fi

if [ -n "${PUBLISH:-}" ]; then
    # Publish only from dev. Verify on mark -> promote mark->dev -> checkout dev.
    CUR_BRANCH="$(git -C "$FREEEED_REPO" rev-parse --abbrev-ref HEAD 2>/dev/null || echo unknown)"
    if [ "$CUR_BRANCH" != "dev" ]; then
        echo "ERROR: PUBLISH refused - FreeEed repo is on '$CUR_BRANCH', not 'dev'." >&2
        echo "       Publish only from dev: verify on mark, promote mark->dev," >&2
        echo "       'git checkout dev' in $FREEEED_REPO, then PUBLISH=1 ./release.sh." >&2
        exit 1
    fi
    unset NO_UPLOAD
    MODE_DESC="PUBLISH: on 'dev', this build WILL upload to S3 (publishes the daily)."
else
    # Development build: never uploads, and only builds this machine's installer.
    export NO_UPLOAD=1
    case "$(uname -s)" in
        Darwin) export MAC_ONLY=1;   MODE_DESC="Dev build: macOS .dmg only, NO S3 upload." ;;
        Linux)  export LINUX_ONLY=1; MODE_DESC="Dev build: Linux .run only, NO S3 upload." ;;
        *)      MODE_DESC="Dev build: unrecognized platform '$(uname -s)'; building what this machine can, NO S3 upload." ;;
    esac
fi

echo ">>> $MODE_DESC"

if [ -n "${RELEASE_DRY_RUN:-}" ]; then
    echo "--- dry run, not building ---"
    echo "FREEEED_REPO = $FREEEED_REPO"
    echo "SHMSOFT_HOME = $SHMSOFT_HOME"
    echo "SCAIA_HOME   = ${SCAIA_HOME:-(unset)}"
    echo "NO_UPLOAD    = ${NO_UPLOAD:-(unset)}"
    echo "MAC_ONLY     = ${MAC_ONLY:-(unset)}"
    echo "LINUX_ONLY   = ${LINUX_ONLY:-(unset)}"
    echo "branch       = $(git -C "$FREEEED_REPO" rev-parse --abbrev-ref HEAD 2>/dev/null || echo unknown)"
    exit 0
fi

exec "$REAL_RELEASE" "$@"
