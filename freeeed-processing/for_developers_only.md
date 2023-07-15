## Requirements

### For development

* Go to the place where you installed complete pack, such as ~/projects/SHMsoft/freeeed_complete_pack
```shell
./start_dev_services.sh
```
* If you do not have that `start_dev_services.sh` there, copy it from this project's root directory
* 

### Pre-compile

* The jars in the 'lib' directory should be  imported into the local .m2 repository

* These instructions assume that you are in the `freeeed-processing` project directory

### Linux

#### IBM Lotus Notus a.k.a Domino

<pre>mvn install:install-file -DgroupId="com.ibm" -DartifactId="notes" -Dversion="7.3.4" -Dfile="lib/Notes.jar" -Dpackaging="jar" -DgeneratePom="true"</pre>

#### JPST

```shell
mvn install:install-file -DgroupId="com.independentsoft" -DartifactId="JPST" -Dversion=1.0 -Dfile="lib/jpst.jar" -Dpackaging=jar -DgeneratePom=true</pre>
```


### Windows

#### IBM Lotus Notus a.k.a Domino

```shell
mvn install:install-file -DgroupId="com.ibm" -DartifactId="notes" -Dversion="7.3.4" -Dfile="lib\Notes.jar" -Dpackaging="jar" -DgeneratePom="true"</pre>
```

#### JPST

```shell
mvn install:install-file -DgroupId="com.independentsoft" -DartifactId="JPST" -Dversion=1.0 -Dfile="lib\jpst.jar" -Dpackaging=jar -DgeneratePom=true</pre>
```


### FreeEedReview

If you want to make your build with FreeEedReview included you must clone from our repo

It must be next to FreeEED folder

<pre>git clone https://github.com/shmsoft/FreeEedUI/</pre>

Checkout branch solr-branch

<pre>git checkout solr-branch</pre>

after all before starting `release_freeeed_complete.sh` you have to do the following (only if you are on linux)

<pre> dos2unix release_freeeed_complete.sh </pre>

When testing, if you plan to use JPST, do the target assembly:single first

How to create your first 'settings.properties'

Copy settings-template.properties to settings.properties.

NOTE that settings.properties is ignored by git and will not be committed. It is safe to put your secret information
there - such as Amazon keys.

For PST processing, normally you would use readpst. JPST is a special case for Windows. 

To install readpst, go here https://github.com/shmsoft/FreeEed/wiki/FreeEed-Installation



