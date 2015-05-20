echo off

unset CATALINA_HOME
unset CATALINA_BASE
chmod 755 -R freeeed-tomcat
cd freeeed-tomcat/bin;
./startup.sh &

cd ../..

cd freeeed-solr/example
java -Xmx1024M -jar start.jar &

cd ../..

cd FreeEed
./freeeed_player.sh &