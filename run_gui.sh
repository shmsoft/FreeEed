#!/bin/sh
#check for update
arfile="target/SHMcloud-1.0-SNAPSHOT-jar-with-dependencies.jar"
newjarfile="target/SHMcloud-1.0-SNAPSHOT-jar-with-dependencies.jar.new"
oldjarfile="target/SHMcloud-1.0-SNAPSHOT-jar-with-dependencies.jar.old"
if [ -f $jarfile.new ]
then
    echo "upgrading to new version of SHMcloud"
    mv $jarfile $oldjarfile
    mv $newjarfile $jarfile
else
    echo "no update found, running current code"
fi
java -Xms512m -Xmx1024m  -cp target/FreeEed-1.0-SNAPSHOT-jar-with-dependencies.jar:drivers/truezip-driver-zip-7.3.4.jar org.freeeed.ui.FreeEedUI $1
