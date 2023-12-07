echo off

unset CATALINA_HOME
unset CATALINA_BASE
chmod -R 755 freeeed-tomcat
# fix for Mac
chmod u+x freeeed-tomcat/bin/*.sh
cd freeeed-tomcat/bin;
./startup.sh &
cd ../..

cd freeeed-solr/example
java -Xmx1024M -jar start.jar &
cd ../..

ulimit -Sn 4000
cd freeeed-tika
java -Xmx1024M -jar tika-server.jar &
cd ..


cd FreeEed
./freeeed_player.sh &
