#!/bin/sh

java -Xms512m -Xmx1024m -Dlog4j.configuration=file:"config/log4j.properties" \
-cp target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar:drivers/truezip-driver-zip-7.7.4.jar \
org.freeeed.piranha.Investigate $1
