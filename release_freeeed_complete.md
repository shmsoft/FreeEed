# Documentation for doing the release 
* The release script is `release_freeeed_complete.sh`

### How to run the release script
* The following variables have to be set in your environment
* `SHMSOFT_HOME` - place where FreeEed and FreeEedUI projects are
* `ZIP_PASS` - password for the resulting zip file
* AWS keys to be set up in your environment
* You need to set up the version in the script


### How to do the release

* You invoke this script in whatever directory you like. (It is easier to run it from the release directory)
  * This script takes the current version of the code from directories, does not do anything with Git
  * The results will be found in the release folder, named after version
  * For example, if your `SHMSOFT_HOME` is `/home/mark/projects/SHMsoft` then
    * You can run in any directory
    * FreeEed and FreeEedUI should be under `/home/mark/projects/SHMsoft`
    * Release artifacts will be under `/home/mark/projects/SHMsoft/release`
    * For version 9.0.0, you will see `/home/mark/projects/SHMsoft/release/9.0.0`
