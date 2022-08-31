-- to run: hive -f hive_create_table.sql
DROP TABLE IF EXISTS load_file;
create external table load_file (Hash string,no_value string,UPI string,File_Name string,Custodian string,Source_Device string,Source_Path string,Production_Path string,Modified_Date string,Modified_Time string,Time_Offset_Value string,processing_exception string,master_duplicate string,text string,Email_To string,Email_From string,Email_CC string,Email_BCC string,Date_Sent string,Time_Sent string,Subject string,Date_Received string,Time_Received string)
row format delimited
fields terminated by '|'
stored as textfile