#!/usr/bin/env bash
if [ -z "${ZIP_PASS}" ]; then echo Zip password not set; exit; fi

export PROJECT_DIR=$HOME/projects/SHMsoft
export FREEEED_PROJECT=$PROJECT_DIR/FreeEed
export FREEEED_UI_PROJECT=$PROJECT_DIR/FreeEedUI

#============================ user setup ==================================

export UPLOAD_TO_S3_FREEEED_PLAYER=yes
export UPLOAD_TO_S3_FREEEED_UI=yes
export UPLOAD_TO_S3_FREEEED_PACK=yes
export BUILD_FREEEED_PLAYER=yes
export BUILD_FREEEED_UI=yes
export BUILD_FREEEED_PACK=yes

export VERSION=7.7.0

rm -rf $VERSION
mkdir $VERSION
cd $VERSION

export CURR_DIR=`pwd`
echo "Working dir: $CURR_DIR"

if [ "${BUILD_FREEEED_PLAYER}" ]; then

    echo "FreeEed: GIT pull"
    cd $FREEEED_PROJECT;git pull;
    
    echo "FreeEed: mvn clean install"
    cd $FREEEED_PROJECT/freeeed-processing;mvn clean install;
    
    echo "FreeEed: mvn package assembly:single"
    cd $FREEEED_PROJECT/freeeed-processing;mvn package assembly:single;
    
    cd $CURR_DIR
    mkdir tmp;
    cd tmp;
    
    echo "FreeEed: Copying resources to: $CURR_DIR/tmp"
    cp -R $FREEEED_PROJECT/freeeed-processing FreeEed
    cp -R $FREEEED_PROJECT/test-data .
    cd FreeEed

    cp src/main/resources/log4j.properties config/

    chmod +x prepare-clean-for-release.sh        
    
    echo "FreeEed: cleaning up...."
    ./prepare-clean-for-release.sh        

    cp settings-template.properties settings.properties
    sed -i '/download-link/d' settings.properties
    echo "download-link=http://shmsoft.s3.amazonaws.com/releases/FreeEed-$VERSION.zip" >> settings.properties
    dos2unix config/hadoop-env.sh

    cd $CURR_DIR/tmp
    
    echo "FreeEed: Creating zip file"
    zip -P $ZIP_PASS -r FreeEed-$VERSION.zip FreeEed
    cd $CURR_DIR
    mv tmp/FreeEed-$VERSION.zip .
    
    echo "FreeEed: Done -- `ls -la FreeEed-*.zip`"
fi

if [ "${BUILD_FREEEED_UI}" ]; then
    echo "FreeEed UI: GIT pull"
    cd $FREEEED_UI_PROJECT;git pull;
    sed -i "s/version: [0-9].[0-9].[0-9]/version: $VERSION/" $FREEEED_UI_PROJECT/src/main/webapp/template/header.jsp
    cd $CURR_DIR
    cp -R $FREEEED_UI_PROJECT FreeEedUI
    
    echo "FreeEed UI: creating war file"
    cd FreeEedUI;mvn clean install war:war
    
    cd $CURR_DIR
    cp FreeEedUI/target/freeeedui*.war .
    mv freeeedui*.war freeeedui-$VERSION.war
    rm -rf FreeEedUI
    
    echo "FreeEed UI: Done -- `ls -la freeeedui*.war`"
fi

if [ "${BUILD_FREEEED_PACK}" ]; then
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
    
    cp $FREEEED_PROJECT/start_all.bat .
    cp $FREEEED_PROJECT/start_all.sh .
    
    cd $CURR_DIR
    mv tmp freeeed_complete_pack
    zip -P $ZIP_PASS -r freeeed_complete_pack-$VERSION.zip freeeed_complete_pack
    
    echo "Done -- `ls -la freeeed_complete*.zip`"
fi

if [ "${UPLOAD_TO_S3_FREEEED_PLAYER}" ]; then
    echo "Uploading $VERSION/FreeEed-$VERSION.zip to s3://shmsoft/releases/"
    echo "CURR_DIR=" $CURR_DIR
    cd $CURR_DIR
    s3cmd -P put FreeEed-$VERSION.zip s3://shmsoft/releases/
fi

if [ "${UPLOAD_TO_S3_FREEEED_UI}" ]; then
    echo "Uploading to S3.... freeeedui-$VERSION.war"
    cd $CURR_DIR
    s3cmd -P put freeeedui-$VERSION.war s3://shmsoft/releases/
fi

if [ "${UPLOAD_TO_S3_FREEEED_PACK}" ]; then
    echo "Uploading to S3.... freeeed_complete_pack-$VERSION.zip"
    cd $CURR_DIR
    s3cmd -P put freeeed_complete_pack-$VERSION.zip s3://shmsoft/releases/
fi

echo "Upload Done!"




