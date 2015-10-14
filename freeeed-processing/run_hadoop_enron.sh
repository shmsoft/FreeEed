#!/bin/sh
hadoop fs -rmr /freeeed/output
hadoop jar \
target/FreeEed-1.0-SNAPSHOT-jar-with-dependencies.jar org.freeeed.mr.FreeEedMR \
-libjars drivers/truezip-driver-zip-7.7.4.jar \
hadoop_test_s3.project \
/freeeed/output
