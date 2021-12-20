echo "Stopping Tomcat"
unset CATALINA_HOME
unset CATALINA_BASE
cd freeeed-tomcat/bin;
./shutdown.sh &

echo "List java processes as follows"
echo "ps aux | grep java | grep start.jar"
ps aux | grep java | grep start.jar
echo "Then kill the process by id"
