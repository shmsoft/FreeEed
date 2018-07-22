echo off

cd freeeed-tomcat\bin

startup.bat
cd ..\..


cd elasticsearch-6.2.2\bin
start elasticsearch.bat


cd ..\..


cd FreeEed

start freeeed_player.bat