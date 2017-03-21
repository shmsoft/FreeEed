if [ $# -eq 0 ]
  then
    echo "Please supply output directory name"
    exit
fi
directory=$1
echo "directory=$directory"
for file_name in `ls $directory`; do
    file_start=${file_name%_*} 
    rename_to=$file_start".zip"
    echo "renaming $directory/$file_name to $directory/$rename_to"
    mv $directory/$file_name $directory/$rename_to
done
