
#!/usr/bin/env bash
set -e

# Always run from this script's directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

source myenv/bin/activate
exec python -m uvicorn main:app --reload

