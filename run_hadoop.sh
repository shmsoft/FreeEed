#!/bin/sh
# small_hadoop.project
hadoop fs -rmr /freeeed/output
hadoop jar \
target/FreeEed-1.0-SNAPSHOT-jar-with-dependencies.jar org.freeeed.main.MRFreeEedProcess \
-libjars drivers/truezip-driver-zip-7.3.4.jar -D Xmx1024m -D mapred.reduce.tasks=4 \
sample_hadoop.project \
/freeeed/output

hive -f scripts/hive_create_table.sql 
hive -f scripts/hive_load_table.sql 
