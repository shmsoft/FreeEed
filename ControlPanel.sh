#!/bin/bash
# Wrapper script to run the Control Panel on Mac/Linux

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Launch the Control Panel using Java.
# We include the processing jar in the classpath
java -cp "FreeEed/target/*:FreeEed/target/lib/*:FreeEed/target/dependency/*:FreeEed/*" org.freeeed.ui.ControlPanelUI
