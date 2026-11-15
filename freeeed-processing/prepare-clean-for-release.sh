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
# Developer-only fixtures: in the repo for people working on the code, out of
# the installer that customers download. The pack copies freeeed-processing
# wholesale, so test-data ships unless it is pruned here.
#   loadfile_test  137 MB, 4962 files -- DAT/OPT import fixtures (#599, #623).
#                  No end user will ever open them.
#   pst             14 MB -- redundant: 03-enron-pst already ships and is the
#                  one the bundled sample project points at.
# Keep 01-one-time-test, 02-loose-files and 03-enron-pst: they are custodians
# c1/c2/c3 of the bundled sample project, so cutting them breaks the
# out-of-the-box demo. Keep 11-foia-corpus: shipping it is the point -- a
# tester on a fresh machine then already has the FOIA corpus.
rm -fr test-data/loadfile_test
rm -fr test-data/pst
rm -rf *.xml
rm -fr tmp
rm -rf install_jpst_cygwin.bat
rm -rf settings.properties
rm -rf target/generated-sources
rm -rf target/maven-archiver
rm -rf target/surefire-reports
rm -rf target/freeeed-processing-1.0-SNAPSHOT-sources.jar
rm -rf target/freeeed-processing-1.0-SNAPSHOT.jar
# Never ship freeeed.db - it is created on first run and can hold user secrets
# (e.g. an OpenAI key). -f so a missing file isn't an error; find catches any nested copy.
rm -f freeeed.db
find . -name 'freeeed.db' -delete 2>/dev/null

echo "Done"
