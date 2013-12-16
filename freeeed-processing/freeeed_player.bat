@echo off
echo ===============================================================
echo FreeEed is checking your system:
echo ===============================================================
"%JAVA_HOME%\bin\java" -version:1.7 -version > nul 2>&1
if %ERRORLEVEL% == 0 goto java7
echo Please install Java 7 and set JAVA_HOME.
PAUSE
goto end

:java7
echo Java 7 found: %JAVA_HOME%
goto office

:office
IF EXIST "C:\Program Files\OpenOffice.org 3" GOTO office3
IF EXIST "C:\Program Files\OpenOffice 4" GOTO office4

echo Open office not found neither in C:\Program Files\OpenOffice.org 3 nor in C:\Program Files\OpenOffice 4. If you have it installed in other directory, please setup it in settings.properties
goto found

:office3
echo Open office found in C:\Program Files\OpenOffice.org 3

:office4
echo Open office found in  C:\Program Files\OpenOffice 4 

:found
echo ===============================================================
"%JAVA_HOME%/bin/java" -Xms512m -Xmx1024m -cp target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar;drivers/truezip-driver-zip-7.3.4.jar org.freeeed.ui.FreeEedUI

:end