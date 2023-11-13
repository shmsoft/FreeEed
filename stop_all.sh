echo "Stopping Tomcat"
unset CATALINA_HOME
unset CATALINA_BASE
cd freeeed-tomcat/bin;
./shutdown.sh &

cd ../..

ps -ef  | grep java | grep start.jar
ps -ef | grep tika | grep Xmx
