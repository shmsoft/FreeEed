# This has to be run from the new release
# Like `cd 10.2.6`
#!/usr/bin/env bash
if [ -z "$1" ]; then
    echo "No IP provided."
    exit 1
fi
scp freeeed_complete_pack/freeeed-tomcat/webapps/freeeedui.war ubuntu:$1:/home/ubuntu/Desktop/freeeed_complete_pack/freeeed-tomcat/webapps/freeeedui.war
scp freeeed_complete_pack/FreeEed/target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar ubuntu:$1:/home/ubuntu/Desktop/freeeed_complete_pack/FreeEed/target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar
