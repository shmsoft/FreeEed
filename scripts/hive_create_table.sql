-- to run: hive -f file-name
create table load_file (Hash string, UPI string, File_Name string, Custodian string, Source_Device string, Source_Path string, Production_Path string, Modified_Date string, Modified_Time string, Time_Offset_Value string, processing_exception string, EMail_To string, EMail_From string, EMail_CC string, EMail_BCC string, Date_Sent string, Time_Sent string, Subject string, Date_Received string, Time_Received string)
row format delimited 
fields terminated by '\t'
stored as textfile

create table load_file (Hash string, UPI string, File_Name string, Custodian string, .... )
row format delimited 
fields terminated by '\t'
stored as textfile