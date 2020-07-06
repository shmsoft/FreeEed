## Requirements

The jars in the 'lib' directory should be  imported into the local .m2 repository, because they are not available anywhere in web-based maven repositories



**These instructions assume that you are in the 'freeeed-processing' project directory**


##IMPORTANT
To run the maven import commands in Windows, you need to be in commandline, in the freeeed-processing directory

Do not forget to set ZIP_PASS env variable if you are planing to do a complete release

<pre>export ZIP_PASS=yourpass</pre>

if any of the following parameter are set to be true you must have s3cmd installed and configured

<pre>
BUILD_FREEEED_PLAYER=true
BUILD_FREEEED_UI=true
BUILD_FREEEED_PACK=true
</pre>

###Linux
#### IBM Lotus Notus a.k.a Domino

<pre>mvn install:install-file -DgroupId="com.ibm" -DartifactId="notes" -Dversion="7.3.4" -Dfile="lib/Notes.jar" -Dpackaging="jar" -DgeneratePom="true"</pre>

#### JPST

<pre>mvn install:install-file -DgroupId="com.independentsoft" -DartifactId="JPST" -Dversion=1.0 -Dfile="lib/jpst.jar" -Dpackaging=jar -DgeneratePom=true</pre>



###Windows
#### IBM Lotus Notus a.k.a Domino

<pre>mvn install:install-file -DgroupId="com.ibm" -DartifactId="notes" -Dversion="7.3.4" -Dfile="lib\Notes.jar" -Dpackaging="jar" -DgeneratePom="true"</pre>

#### JPST

<pre>mvn install:install-file -DgroupId="com.independentsoft" -DartifactId="JPST" -Dversion=1.0 -Dfile="lib\jpst.jar" -Dpackaging=jar -DgeneratePom=true</pre>


<pre>install_jpst_cygwin.bat</pre>


###FreeEEDUI
If you want to make your build with FreeEEDUI included you must clone from our repo

It must be next to FreeEED folder

<pre>git clone https://github.com/shmsoft/FreeEedUI/</pre>

Then fetch all the tags

<pre>git fetch --all --tags</pre>

for now, please use V7.0.0 for review, checkout tag v7.0.0

<pre>git checkout tags/V7.0.0</pre> 

after all before starting `release_freeeed_complete.sh` you have to do the following (only if you are on linux)

<pre> dos2unix release_freeeed_complete.sh </pre>

When testing, if you plan to use JPST, do the target assembly:single first

How to create your first 'settings.properties'

Copy settings-template.properties to settings.properties.

NOTE that settings.properties is ignored by git and will not be committed. It is safe to put your secret information
there - such as Amazon keys.

For PST processing, normally you would use readpst. JPST is a special case for Windows. 

To install readpst, go here https://github.com/shmsoft/FreeEed/wiki/FreeEed-Installation
