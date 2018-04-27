echo off

unset CATALINA_HOME
unset CATALINA_BASE
chmod -R 755 freeeed-tomcat
# fix for Mac
chmod u+x freeeed-tomcat/bin/*.sh
cd freeeed-tomcat/bin;
./startup.sh &

cd ../..

cd elasticsearch-6.2.2
./bin/elasticsearch
