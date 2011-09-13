#!/bin/sh
echo "Stop! Are you sure you want to clean all that stuff? That's only for release, you know!" - y/n
read WISH
echo
if [ $WISH != "y" ] ; then
echo you chose no, good bye 
exit
fi
# Now let's go and delete
echo "Cleaning..."
rm -fr build
rm build.xml
rm -fr nbproject
rm -fr logs
rm -fr pst_output
rm -fr freeeed_output
rm -fr freeeed_download
rm manifest.mf
rm -fr src
rm -fr test
rm -fr lib
rm -fr .git
rm .gitignore
rm prepare-clean-for-release.sh
echo "Done"
