@echo off
"%JAVA_HOME%\bin\java" -version:1.7 -version > nul 2>&1
if %ERRORLEVEL% == 0 goto found
echo Please install Java 7 and set JAVA_HOME.
PAUSE
goto end

:found
"%JAVA_HOME%/bin/java" -Xms512m -Xmx1024m -cp target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar;drivers/truezip-driver-zip-7.3.4.jar org.freeeed.ui.FreeEedUI

:end