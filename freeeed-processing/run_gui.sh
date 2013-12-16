echo ===============================================================
echo FreeEed is checking your system:
echo ===============================================================

if type -p java; then
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then     
    _java="$JAVA_HOME/bin/java"
else
    echo "no java found either in PATH nor in JAVA_HOME"
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo Java version: $version
    if [[ "$version" > "1.7" ]]; then
        _java7="$version"
    else         
        echo "Your jave is too old. Please install Java 7 to run FreeEed"
    fi
fi

if [ -d "/opt/openoffice.org4" ]; then
  echo Open Office found in /opt/openoffice.org4
elif [ -d "/opt/openoffice.org3" ]; then
  echo Open Office found in /opt/openoffice.org3
else
  echo Open office not found neither in /opt/openoffice.org4 nor in /opt/openoffice.org3. If you have it installed in other directory, please setup it in settings.properties
fi

pstversion=$(readpst -V 2>&1 | grep 'LibPST' | awk '{split($0,array," ")} END{print array[4]}' | sed s/^.//)
if [[ "$pstversion" < "0.6.61" ]]; then
   echo "You don't have correct readpst version. Required version is: 0.6.61"
else
   echo "readpst version: $pstversion"
fi


echo ===============================================================

if [[ "$_java7" ]]; then
  java -Xms512m -Xmx1024m  -cp target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar:drivers/truezip-driver-zip-7.7.4.jar org.freeeed.ui.FreeEedUI $1
fi

