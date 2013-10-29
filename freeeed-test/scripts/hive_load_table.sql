-- to run: hive -f hive_load_table.sql
load data inpath '/freeeed/output/part-r*'
overwrite into table load_file;

-- to verify, enter hive shell, then
-- select count (*) from load_file; 
