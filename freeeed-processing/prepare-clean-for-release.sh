#!/bin/sh
#echo "Stop! Are you sure you want to clean all that stuff? That's only for release, you know!" - y/n
#read WISH
#echo
#if [ $WISH != "y" ] ; then
#echo you chose no, good bye 
#exit
#fi
# Now let's go and delete
echo "Cleaning..."
rm -fr build
rm -fr nbproject
rm -fr logs
rm -fr pst_output
rm -fr freeeed-output
rm -fr output*
rm -fr freeeed_download
rm -fr data_downloads
rm -fr src
rm -fr test
rm -fr lib
rm -fr .git
rm -fr test-data/staged
rm -rf *.xml
rm -fr tmp
rm -rf install_jpst_cygwin.bat
rm -rf settings.properties
rm -rf target/generated-sources
rm -rf target/maven-archiver
rm -rf target/surefire-reports
rm -rf target/freeeed-processing-1.0-SNAPSHOT-sources.jar
rm -rf target/freeeed-processing-1.0-SNAPSHOT.jar
rm freeeed.db

echo "Done"
