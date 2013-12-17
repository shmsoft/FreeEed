#!/bin/sh
# parameters: 1 - project, 2 - output dir, 3 - number of reducers
hadoop fs -rmr $2
hadoop jar \
target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar org.freeeed.main.MRFreeEedProcess \
-libjars drivers/truezip-driver-zip-7.7.4.jar -D Xmx3096m -D mapred.reduce.tasks=$3 \
$1 $2
