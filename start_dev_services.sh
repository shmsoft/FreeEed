# this script should be run from freeeed_complete_pack
echo "******************** this script should be run from freeeed_complete_pack"

#
echo off

unset CATALINA_HOME
unset CATALINA_BASE
chmod -R 755 freeeed-tomcat
# fix for Mac
# chmod u+x freeeed-tomcat/bin/*.sh
#cd freeeed-tomcat/bin;
#./startup.sh &
#cd ../..

cd freeeed-solr/example
java -Xmx1024M -jar start.jar &
cd ../..

cd freeeed-tika
java -Xmx1024M -jar tika-server.jar &
cd ..

