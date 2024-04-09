# Upgrade SKY VM
#!/usr/bin/env bash
if [ -z "$1" ]; then
    echo "No IP provided."
    exit 1
fi
scp -i ~/.ssh/freeeed_admin.pem ~/projects/SHMsoft/freeeed_complete_pack/freeeed-tomcat/webapps/freeeedui.war freeeed@$1:/home/freeeed/Desktop/freeeed_complete_pack/freeeed-tomcat/webapps/
scp -i ~/.ssh/freeeed_admin.pem ~/projects/SHMsoft/freeeed_complete_pack/FreeEed/target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar freeeed@$1:/home/freeeed/Desktop/freeeed_complete_pack/FreeEed/target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar
scp -i ~/.ssh/freeeed_admin.pem  ~/projects/scaia/AIAdvisor/code/python/* freeeed@$1:~/projects/scaia/AIAdvisor/code/python/
