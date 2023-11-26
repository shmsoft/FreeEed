#!/usr/bin/env bash
# Go to a special directory designated to hold all developer releases
# There, run this script, giving its complete path.
# The release will create its own directory, as shown by `VersionNumber.txt` below

if [ -z "${ZIP_PASS}" ]; then
  echo Zip password not set
  exit
fi

if [ -z "${SHMSOFT_HOME}" ]; then
  echo SHMSoft_HOME not set
  exit
fi

PROJECT_DIR=$SHMSOFT_HOME
RELEASE_DIR=$PROJECT_DIR/release
FREEEED_PROJECT=$PROJECT_DIR/FreeEed
FREEEED_UI_PROJECT=$PROJECT_DIR/FreeEedUI
VERSION=10.0.7
echo "Building version "$VERSION

#============================ user setup ==================================

UPLOAD_TO_S3_FREEEED_PLAYER=false
UPLOAD_TO_S3_FREEEED_UI=true
UPLOAD_TO_S3_FREEEED_PACK=true

BUILD_FREEEED_PLAYER=true
BUILD_FREEEED_UI=true
BUILD_FREEEED_PACK=true

cd $SHMSoft_HOME || exit
mkdir -p $RELEASE_DIR
cd $RELEASE_DIR || exit
rm -rf $VERSION
mkdir -p $VERSION
cd $VERSION || exit

CURR_DIR=$(pwd)

echo "Working dir: $CURR_DIR"

if [ "$BUILD_FREEEED_PLAYER" == true ]; then

  echo "FreeEed: mvn clean install"
  cd $FREEEED_PROJECT/freeeed-processing || exit
  mvn clean install;

  echo "FreeEed: mvn package assembly:single"
  cd $FREEEED_PROJECT/freeeed-processing || exit
  mvn package assembly:single

  cd $CURR_DIR || exit
  mkdir tmp
  cd tmp || exit

  echo "FreeEed: Copying resources to: $CURR_DIR/tmp"
  cp -R $FREEEED_PROJECT/freeeed-processing FreeEed
  cd FreeEed || exit

  cp src/main/resources/log4j.properties config/

  chmod +x prepare-clean-for-release.sh

  echo "FreeEed: cleaning up...."
  # dos2unix prepare-clean-for-release.sh
  ./prepare-clean-for-release.sh

  cp settings-template.properties settings.properties
  sed -i '/download-link/d' settings.properties
  echo "download-link=http://shmsoft.s3.amazonaws.com/releases/FreeEed-$VERSION.zip" >>settings.properties
  dos2unix config/hadoop-env.sh

  cd $CURR_DIR/tmp || exit

  echo "FreeEed: Creating zip file"
  zip -P $ZIP_PASS -r FreeEed-$VERSION.zip FreeEed
  cd $CURR_DIR || exit
  mv tmp/FreeEed-$VERSION.zip .

  echo "FreeEed: Done -- $(ls -la FreeEed-*.zip)"

fi


if [ "$BUILD_FREEEED_UI" == true ]; then
    cd $CURR_DIR || exit
    cp -R $FREEEED_UI_PROJECT FreeEedUI

    echo "FreeEed UI: creating war file"
    cd FreeEedUI || exit;
    mvn clean install war:war

    cd $CURR_DIR || exit
    cp FreeEedUI/target/freeeedui*.war .
    mv freeeedui*.war freeeedui-$VERSION.war

    echo "FreeEed UI: Done -- `ls -la freeeedui*.war`"
fi

if [ "$BUILD_FREEEED_PACK" == true ]; then
    cd $CURR_DIR/tmp || exit

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

    cp $FREEEED_PROJECT/start_all.bat .
    cp $FREEEED_PROJECT/start_all.sh .
    cp $FREEEED_PROJECT/stop_all.sh .
    cp $FREEEED_PROJECT/stop_dev_services.sh .
    cp $FREEEED_PROJECT/start_dev_services.sh .

    echo "Downloading tika-server... "
    wget https://s3.amazonaws.com/shmsoft/release-artifacts/tika-server-standard-2.4.1.jar
    mkdir freeeed-tika/
    mv tika-server-standard-2.4.1.jar freeeed-tika/tika-server.jar

    cp $FREEEED_PROJECT/start_all.bat .
    cp $FREEEED_PROJECT/start_all.sh .
    cp $FREEEED_PROJECT/stop_all.sh .

    cd $CURR_DIR || exit
    mv tmp freeeed_complete_pack
    zip -P $ZIP_PASS -r freeeed_complete_pack-$VERSION.zip freeeed_complete_pack

    echo "Done -- `ls -la freeeed_complete*.zip`"
fi

if [ "$UPLOAD_TO_S3_FREEEED_PLAYER" == true ]; then
    echo "Uploading $VERSION/FreeEed-$VERSION.zip to s3://shmsoft/releases/"
    echo "CURR_DIR=" $CURR_DIR
    cd $CURR_DIR || exit
    aws s3 cp FreeEed-$VERSION.zip s3://shmsoft/releases/
fi

if [ "$UPLOAD_TO_S3_FREEEED_UI" == true ]; then
    echo "Uploading to S3.... freeeedui-$VERSION.war"
    cd $CURR_DIR || exit
    aws s3 cp freeeedui-$VERSION.war s3://shmsoft/releases/
fi

if [ "$UPLOAD_TO_S3_FREEEED_PACK" == true ]; then
    echo "Uploading to S3.... freeeed_complete_pack-$VERSION.zip"
    cd $CURR_DIR || exit
    aws s3 cp freeeed_complete_pack-$VERSION.zip s3://shmsoft/releases/
    aws s3api put-object-acl --bucket shmsoft --key releases/freeeed_complete_pack-$VERSION.zip --acl public-read
fi


echo "Upload Done!"

