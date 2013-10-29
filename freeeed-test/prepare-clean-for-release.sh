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
rm -fr freeeed_download
rm -fr src
rm -fr test
rm -fr lib
rm -fr .git
rm -fr test-data/staged
rm .gitignore
rm *.xml
rm -fr tmp
rm install_jpst_cygwin.bat
rm settings.properties
rm prepare-clean-for-release.sh
echo "Done"
