echo off

cd freeeed-tomcat\bin
start startup.bat

cd ..\..

cd freeeed-solr
bin/solr.cmd -e schemaless

cd ..\..

cd FreeEed
start freeeed_player.bat