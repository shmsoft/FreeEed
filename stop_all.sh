# echo off

unset CATALINA_HOME
unset CATALINA_BASE
cd freeeed-tomcat/bin;
./shutdown.sh &

cd ../..

# TODO - kill ES process by id

cd ../..

# TODO - kill FreeEed process by id
