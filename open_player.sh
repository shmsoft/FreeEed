#!/bin/bash

# Kill exiting FreeEedUI process if running
echo "Stopping any existing FreeEed Player..."
pkill -f "java.*FreeEedUI"

echo "Starting FreeEed Player..."
cd FreeEed
./freeeed_player.sh &
cd ..
