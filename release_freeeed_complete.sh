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
PYTHON_DIR=$PROJECT_DIR/FreeEed/python 
FEATURES_DIR=$SCAIA_HOME/FreeEed-features/releases
VERSION=10.8.1-SNAPSHOT
echo "Building version "$VERSION

#============================ user setup ==================================

UPLOAD_TO_S3_FREEEED_PLAYER=false
UPLOAD_TO_S3_FREEEED_UI=true
UPLOAD_TO_S3_FREEEED_PACK=true

BUILD_FREEEED_PLAYER=true
BUILD_FREEEED_UI=true
BUILD_FREEEED_PACK=true

cd $SHMSOFT_HOME || exit
mkdir -p $RELEASE_DIR
cd $RELEASE_DIR || exit
rm -rf $VERSION
mkdir -p $VERSION
cd $VERSION || exit

CURR_DIR=$(pwd)
INSTALLER_OUTPUT_DIR="$CURR_DIR"

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
    cd "$CURR_DIR/tmp" || exit 1

    RELEASES_SRC="$SCAIA_HOME/backup_restore/releases"
    AI_ADVISOR_SRC="$SCAIA_HOME/ai_advisor/releases"
    RELEASES_DST="$CURR_DIR/tmp/releases"

    mkdir -p "$RELEASES_DST"

    # ---- Copy backup_restore releases ----
    if [ -d "$RELEASES_SRC" ]; then
        echo "Copying additional release files from $RELEASES_SRC into $RELEASES_DST..."
        cp -R "$RELEASES_SRC/." "$RELEASES_DST/"
    else
        echo "Warning: releases directory not found at $RELEASES_SRC, skipping."
    fi

    # ---- Copy ai_advisor releases ----
    if [ -d "$AI_ADVISOR_SRC" ]; then
        echo "Copying additional release files from $AI_ADVISOR_SRC into $RELEASES_DST..."
        cp -R "$AI_ADVISOR_SRC/." "$RELEASES_DST/"
    else
        echo "Warning: releases directory not found at $AI_ADVISOR_SRC, skipping."
    fi

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
    cp $FREEEED_PROJECT/stop_all.bat .
    cp $FREEEED_PROJECT/stop_dev_services.sh .
    cp $FREEEED_PROJECT/start_dev_services.sh .
    cp $FREEEED_PROJECT/open_player.sh .
    cp $FREEEED_PROJECT/open_player.bat .

    echo "Downloading tika-server... "
    wget https://shmsoft.s3.us-east-1.amazonaws.com/release-artifacts/tika-server-standard-3.2.3.jar
    mkdir freeeed-tika/
    mv tika-server-standard-3.2.3.jar freeeed-tika/tika-server.jar

    cp $FREEEED_PROJECT/start_all.bat .
    cp $FREEEED_PROJECT/start_all.sh .
    cp $FREEEED_PROJECT/stop_all.sh .
    
    echo "Copying Control Panel scripts..."
    cp $FREEEED_PROJECT/ControlPanel.bat .
    cp $FREEEED_PROJECT/ControlPanel.sh .
    cp $FREEEED_PROJECT/freeeed.png .

    cd $CURR_DIR || exit
    mv tmp freeeed_complete_pack
    zip -P $ZIP_PASS -r freeeed_complete_pack-$VERSION.zip freeeed_complete_pack

    echo "Done -- `ls -la freeeed_complete*.zip`"

    echo "FreeEed: Generating OS-Specific Installers..."
    
    # 1. macOS DMG Installer
    if command -v hdiutil &> /dev/null; then
        echo "Creating macOS .dmg installer..."
        hdiutil create -volname "FreeEed-$VERSION" -srcfolder freeeed_complete_pack -ov -format UDZO "$INSTALLER_OUTPUT_DIR/FreeEed-$VERSION-macOS.dmg"
    else
        echo "Warning: hdiutil not found (only available on macOS). Skipping macOS .dmg generation."
    fi
    
    # 2. Windows NSIS Installer
    if command -v makensis &> /dev/null; then
        echo "Creating Windows .exe installer..."
        cp $FREEEED_PROJECT/freeeed_windows_installer.nsi freeeed_complete_pack/
        cd freeeed_complete_pack || exit
        makensis -DVERSION=$VERSION freeeed_windows_installer.nsi
        mv FreeEed-$VERSION-Windows.exe "$INSTALLER_OUTPUT_DIR/"
        cd .. || exit
    else
        echo "Warning: makensis not found. Skipping Windows installer generation."
    fi

    # 3. Linux Makeself Installer
    if command -v makeself &> /dev/null; then
        echo "Creating Linux .run installer..."
        cp $FREEEED_PROJECT/linux_install.sh freeeed_complete_pack/
        chmod +x freeeed_complete_pack/linux_install.sh
        makeself freeeed_complete_pack/ "$INSTALLER_OUTPUT_DIR/FreeEed-$VERSION-Linux.run" "FreeEed E-Discovery Platform" ./linux_install.sh
    else
        echo "Warning: makeself not found. Skipping Linux installer generation."
    fi

fi

if [ "$UPLOAD_TO_S3_FREEEED_PLAYER" == true ]; then
    echo "Uploading $VERSION/FreeEed-$VERSION.zip to s3://shmsoft/releases/"
    echo "CURR_DIR=" $CURR_DIR
    cd $CURR_DIR || exit
    aws s3 cp FreeEed-$VERSION.zip s3://shmsoft/releases/ --profile shmsoft 
fi

if [ "$UPLOAD_TO_S3_FREEEED_UI" == true ]; then
    echo "Uploading to S3.... freeeedui-$VERSION.war"
    cd $CURR_DIR || exit
    aws s3 cp freeeedui-$VERSION.war s3://shmsoft/releases/ --profile shmsoft
fi

if [ "$UPLOAD_TO_S3_FREEEED_PACK" == true ]; then
    echo "Uploading to S3.... freeeed_complete_pack-$VERSION.zip"
    cd $CURR_DIR || exit
    aws s3 cp freeeed_complete_pack-$VERSION.zip s3://shmsoft/releases/ --profile shmsoft
    aws s3api put-object-acl --bucket shmsoft --key releases/freeeed_complete_pack-$VERSION.zip --acl public-read --profile shmsoft

    echo "Uploading Installers to S3..."
    if [ -f "FreeEed-$VERSION-macOS.dmg" ]; then
        aws s3 cp FreeEed-$VERSION-macOS.dmg s3://shmsoft/releases/ --profile shmsoft
        aws s3api put-object-acl --bucket shmsoft --key releases/FreeEed-$VERSION-macOS.dmg --acl public-read --profile shmsoft
    fi
    if [ -f "FreeEed-$VERSION-Windows.exe" ]; then
        aws s3 cp FreeEed-$VERSION-Windows.exe s3://shmsoft/releases/ --profile shmsoft
        aws s3api put-object-acl --bucket shmsoft --key releases/FreeEed-$VERSION-Windows.exe --acl public-read --profile shmsoft
    fi
    if [ -f "FreeEed-$VERSION-Linux.run" ]; then
        aws s3 cp FreeEed-$VERSION-Linux.run s3://shmsoft/releases/ --profile shmsoft
        aws s3api put-object-acl --bucket shmsoft --key releases/FreeEed-$VERSION-Linux.run --acl public-read --profile shmsoft
    fi
fi


echo "Upload Done!"
