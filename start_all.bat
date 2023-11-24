echo off

cd FreeEed
start freeeed_player.bat

cd ..

cd freeeed-tomcat\bin
start startup.bat

cd ..\..

cd freeeed-solr\example
start java -Xmx1024M -jar start.jar

cd ..\..

cd freeeed-tika
start java -Xmx1024M -jar tika-server.jar
cd ..



