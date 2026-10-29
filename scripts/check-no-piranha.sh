#!/usr/bin/env bash
# Leak guard: the new Piranha engine is PRIVATE (github.com/Scaia-ai/piranha) and must
# NEVER be committed to this public open-source repo. This blocks Piranha *implementation*
# paths and the old FreeEed<->piranha sync scripts. It is PATH-based, not word-based, so
# design docs under docs/ that merely mention "piranha" are fine.
#
# Usage: check-no-piranha.sh <file> [<file> ...]   (pass the ADDED/MODIFIED files)
# Exits non-zero and lists offenders if any forbidden path is present.
set -euo pipefail
bad=()
for f in "$@"; do
  [ -z "$f" ] && continue
  case "$f" in
    */org/freeeed/piranha/*|org/freeeed/piranha/*) bad+=("$f") ;;
    *PiranhaProcessor.java|*PiranhaProcessor.class) bad+=("$f") ;;
    */piranha/*|piranha/*) bad+=("$f") ;;                       # a Piranha module dropped into the tree
    *push-freeeed-to-piranha.sh|*push-piranha-to-freeeed.sh|*pull-freeeed-to-piranha.sh) bad+=("$f") ;;
    *piranha*.jar) bad+=("$f") ;;
    *FreeEed-Piranha) bad+=("$f") ;;
  esac
done
if [ ${#bad[@]} -gt 0 ]; then
  echo "ERROR: Piranha is PRIVATE and must not enter the public FreeEed repo." >&2
  printf '  blocked: %s\n' "${bad[@]}" >&2
  echo "Piranha lives in github.com/Scaia-ai/piranha and plugs in via the executor SPI." >&2
  echo "Design docs belong under docs/ (they won't match an implementation path)." >&2
  exit 1
fi
echo "check-no-piranha: OK (${#@} file(s) checked, none forbidden)"
