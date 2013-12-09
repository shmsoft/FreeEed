echo off
echo Checking for updates...
set jarfile="target\SHMcloud-1.0-SNAPSHOT-jar-with-dependencies.jar"
set newjarfile="target\SHMcloud-1.0-SNAPSHOT-jar-with-dependencies.jar.new"
set oldjarfile="target\SHMcloud-1.0-SNAPSHOT-jar-with-dependencies.jar.old"
IF NOT EXIST %jarfile%.new GOTO RUNPROG
echo Updating to the new version of SHMcloud
copy %jarfile% %oldjarfile%
copy %newjarfile% %jarfile%
del %newjarfile%
:RUNPROG
java -Xms512m -Xmx1024m -cp target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar;drivers/truezip-driver-zip-7.3.4.jar org.freeeed.ui.FreeEedUI
