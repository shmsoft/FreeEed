#!/bin/sh

java -Xms512m -Xmx1024m \
-cp target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar \
org.freeeed.piranha.Investigate $1
