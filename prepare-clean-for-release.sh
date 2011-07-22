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
rm prepare-clean-for-release.sh
rm -fr build
rm build.xml
rm -fr nbproject
rm -fr logs
rm -fr pst_output
rm -fr test-output
rm manifest.mf
rm history.txt
rm -fr src
rm -fr test
echo "Done"
