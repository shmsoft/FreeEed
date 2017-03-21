# Run this script in the directory of your choice
# It will download all enron from the FreeEed's S3
# Give it one argument - directory into which to put the files
if [ $# -eq 0 ]
  then
    echo "Please supply output directory name"
    exit
fi
directory=$1
echo "directory=$directory"
rm -fr $directory
mkdir -p $directory
prefix=https://s3.amazonaws.com/shmsoft/enron/
while read file_name; do
  echo $file_name
  file_to_download=$prefix$file_name
  echo "will wget $file_to_download"
  wget $file_to_download -P $directory
done <enron_files.txt




