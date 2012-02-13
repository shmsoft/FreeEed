#!/bin/sh
hadoop fs -rmr /freeeed/output
hadoop jar \
target/FreeEed-1.0-SNAPSHOT-jar-with-dependencies.jar org.freeeed.main.MRFreeEedProcess \
-libjars drivers/truezip-driver-zip-7.3.4.jar \
small_hadoop_test_s3.project \
/freeeed/output
