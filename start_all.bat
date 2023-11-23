echo off

cd freeeed-tomcat\bin
start startup.bat

cd ..\..

cd freeeed-solr\example
start java -Xmx1024M -jar start.jar

cd ..\..

cd freeeed-tika
java -Xmx1024M -jar tika-server.jar &
cd ..
cd ..

cd FreeEed
start freeeed_player.bat

