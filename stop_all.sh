# echo off

unset CATALINA_HOME
unset CATALINA_BASE
cd freeeed-tomcat/bin;
./shutdown.sh &

cd ../..

cd freeeed-solr/example
java -Xmx1024M -jar start.jar &

cd ../..

cd FreeEed
./freeeed_player.sh &
