* Running command: rm -fr is bad for performance
* Running command: /Applications/LibreOffice.app/Contents/MacOS/soffice is also bad for performance
* I want to change the log level from the app

## Issues to be addressed on SHMcloud

* ActionStaging.java uses Java 7 way of handling files; do the same for PackageArchive.java
* Add a choice of AMI's to use (officially this is called imageID)
* Verify all operations to use the currently selected imageId only
* Use relative, not absolute paths to store files (don't forget \ or / for system separator)
* When packaging for staging, put the name of the custodian into a comment in a zip
* When packaging for staging, put the SHA-1 signature into a comment in a zip for each file
* 

When you have time...
* Upgrade typica from 1.4 to 1.7.*

Compare
On Mac
[INFO] Reactor Summary:
[INFO]
[INFO] freeeed-parent ..................................... SUCCESS [  1.368 s]
[INFO] freeeed-processing ................................. SUCCESS [ 55.662 s]
[INFO] freeeed-test ....................................... SUCCESS [01:10 min]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 02:07 min
[INFO] Finished at: 2017-11-24T10:57:21-06:00
[INFO] Final Memory: 62M/1089M
[INFO] ------------------------------------------------------------------------

time mvn clean install output

real	1m59.647s
user	4m12.832s
sys	0m22.979s

17-11-24 11:05:27   Project FreeEed sample project started
17-11-24 11:06:41   job finished
17-11-24 11:06:41   job duration: 74 sec
17-11-24 11:06:41   item count: 2477


 