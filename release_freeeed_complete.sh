if [ -z "${ZIP_PASS}" ]; then echo Zip password not set; exit; fi

export PROJECT_DIR=/home/ilazarov/projects
export FREEEED_PROJECT=$PROJECT_DIR/FreeEed
export FREEEED_UI_PROJECT=$PROJECT_DIR/FreeEedUI
export CHANGELOG_FILE=$FREEEED_PROJECT/freeeed-processing/ChangeLog.txt

#============================ user setup ==================================
export VERSION=4.1.0
if grep -q $VERSION "$CHANGELOG_FILE"; then
   echo "This version has been already created! Please check the changelog file and do the nececssary actions";
   exit;
 fi

export RUN_DATE=2013-12-15
export CHANGES="Changes:                                                                            \n
    - change 1                                                                      \n
    - change 2                                                                      \n
    - change 3                                                                      \n
"

export CHANGELOG="
====================================================================================\n
Version: $VERSION Released                                                             \n
\n
Date: $RUN_DATE                                                                \n
\n
$CHANGES
\n
\n
====================================================================================\n
\n
"
#-----------------------------------------------------------------------------

rm -rf $VERSION
mkdir $VERSION
cd $VERSION

export CURR_DIR=`pwd`
echo "Working dir: $CURR_DIR"

echo "FreeEed: GIT pull"
cd $FREEEED_PROJECT;git pull;

echo "FreeEed: mvn clean install"
cd $FREEEED_PROJECT/freeeed-processing;mvn -Pdefault clean install;

echo "FreeEed: mvn package assembly:single"
cd $FREEEED_PROJECT/freeeed-processing;mvn -Pcloudera package assembly:single;

echo "FreeEed: Updating version to: $VERSION"
echo $VERSION > $FREEEED_PROJECT/freeeed-processing/version.txt
echo $CHANGELOG > a.tmp; cat $CHANGELOG_FILE >> a.tmp; cat a.tmp > $CHANGELOG_FILE
rm a.tmp

cd $CURR_DIR
mkdir tmp;
cd tmp;

echo "FreeEed: Copying resources to: $CURR_DIR/tmp"
cp -R ~/projects/FreeEed/freeeed-processing FreeEed
cd FreeEed
chmod +x
chmod +x prepare-clean-for-release.sh

echo "FreeEed: cleaning up...."
./prepare-clean-for-release.sh

cp settings-template.properties settings.properties
sed -i '/download-link/d' settings.properties
echo "download-link=http://shmsoft.s3.amazonaws.com/releases/FreeEed-$VERSION.zip" >> settings.properties

cd $CURR_DIR/tmp

echo "FreeEed: Creating zip file"
zip -P $ZIP_PASS -r FreeEed-$VERSION.zip FreeEed
cd $CURR_DIR
mv tmp/FreeEed-$VERSION.zip .

echo "FreeEed: Done -- `ls -la FreeEed-*.zip`"

echo "FreeEed UI: GIT pull"
cd $FREEEED_UI_PROJECT;git pull;
sed -i "s/version: [0-9].[0-9].[0-9]/version: $VERSION/" $FREEEED_UI_PROJECT/src/main/webapp/template/header.jsp
cd $CURR_DIR
cp -R ~/projects/FreeEedUI FreeEedUI

echo "FreeEed UI: creating war file"
cd FreeEedUI;mvn clean install war:war

cd $CURR_DIR
cp FreeEedUI/target/freeeedui*.war .
mv freeeedui*.war freeeedui-$VERSION.war
rm -rf FreeEedUI

echo "FreeEed UI: Done -- `ls -la freeeedui*.zip`"

cd $CURR_DIR/tmp

echo "Downloading tomcat..."
wget https://s3.amazonaws.com/shmsoft/release-artifacts/freeeed-tomcat.zip

echo "Unzipping tomcat..."
unzip freeeed-tomcat.zip
rm freeeed-tomcat.zip
mv apache-tomcat* freeeed-tomcat
cp ../freeeedui-$VERSION.war freeeed-tomcat/webapps/freeeedui.war

echo "Downloading Solr... "
wget https://s3.amazonaws.com/shmsoft/release-artifacts/freeeed-solr.zip

echo "Unzipping solr... "
unzip freeeed-solr.zip
rm freeeed-solr.zip
mv apache-solr* freeeed-solr

cp ~/projects/FreeEed/start_all.bat .

cd $CURR_DIR
mv tmp freeeed_complete_pack
zip -P $ZIP_PASS -r freeeed_complete_pack-$VERSION.zip freeeed_complete_pack

echo "Done -- `ls -la freeeed_complete*.zip`"

echo "Uploading to S3.... FreeEed-$VERSION.zip"
cd $CURR_DIR
s3cmd -P put FreeEed-$VERSION.zip s3://shmsoft/releases/
echo "Uploading to S3.... freeeedui-$VERSION.war"
s3cmd -P put freeeedui-$VERSION.war s3://shmsoft/releases/
echo "Uploading to S3.... freeeed_complete_pack-$VERSION.zip"
s3cmd -P put freeeed_complete_pack-$VERSION.zip s3://shmsoft/releases/
echo "Upload Done!"

echo "==================================== Status REPORT ============================="
echo "\n"
echo "NOTE: !!!! Please commit the changelog and version files to GIT !!!!"
echo "\n"
echo "Add the following to FreeEed.org downloads page to Available releases"
echo "$RUN_DATE, Version: $VERSION Released

---------------------------------------------

Binaries:

    FreeEed: http://shmsoft.s3.amazonaws.com/releases/FreeEed-$VERSION.zip
    FreeEedUI: http://shmsoft.s3.amazonaws.com/releases/freeeedui-$VERSION.war
    Complete Pack: http://shmsoft.s3.amazonaws.com/releases/freeeed_complete_pack-$VERSION.zip

ChangeLog:
$CHANGES
"



