# echo off

unset CATALINA_HOME
unset CATALINA_BASE
cd freeeed-tomcat/bin;
./shutdown.sh &

cd ../..

cd freeeed-solr/example
# java -Xmx1024M -jar start.jar &
# TODO - kill the process by id?

cd ../..

cd FreeEed
#./freeeed_player.sh &
# TODO - kill the process by id?
