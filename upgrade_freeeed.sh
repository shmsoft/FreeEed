#!/usr/bin/env bash
# Upgrade SKY VM
if [ -z "$1" ]; then
    echo "No IP provided."
    exit 1
fi
# installs
ssh -i ~/.ssh/freeeed_admin.pem -t freeeed@"$1" "sudo rm /etc/apt/preferences.d/mozilla-firefox"
ssh -i ~/.ssh/freeeed_admin.pem -t freeeed@"$1" "sudo apt-get install tesseract-ocr -y"
# updates
scp -i ~/.ssh/freeeed_admin.pem ~/projects/SHMsoft/freeeed_complete_pack/freeeed-tomcat/webapps/freeeedui.war freeeed@"$1":/home/freeeed/Desktop/freeeed_complete_pack/freeeed-tomcat/webapps/
scp -i ~/.ssh/freeeed_admin.pem ~/projects/SHMsoft/freeeed_complete_pack/FreeEed/target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar freeeed@"$1":/home/freeeed/Desktop/freeeed_complete_pack/FreeEed/target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar
scp -i ~/.ssh/freeeed_admin.pem ~/projects/scaia/AIAdvisor/code/python/* freeeed@"$1":~/projects/scaia/AIAdvisor/code/python/
