@echo off
echo ===============================================================
echo FreeEed is checking your system:
echo ===============================================================

if "%JAVA_HOME%" == "" goto no_java_home

:have_java_home
"%JAVA_HOME%/bin/java" -version 2>ab_java_version.txt
goto check_java_version

:no_java_home
echo Please install Java 7 and set JAVA_HOME.
PAUSE
goto end

:check_java_version
for /f "tokens=3" %%g in (ab_java_version.txt) do (
  echo.%%g | findstr /C:"1.7" 1>nul
	if errorlevel 1 (
		goto :wrong_java_version
	) ELSE (
		goto :java7
	)
)

:wrong_java_version
echo Please install Java 7 and set JAVA_HOME.
PAUSE
goto end

:java7
echo Java 7 found: %JAVA_HOME%
rem goto office

REM :office
REM IF EXIST "C:\Program Files\OpenOffice.org 3" GOTO office3
REM IF EXIST "C:\Program Files (x86)\OpenOffice.org 3" GOTO office3
REM IF EXIST "C:\Program Files\OpenOffice 4" GOTO office4
REM IF EXIST "C:\Program Files (x86)\OpenOffice 4" GOTO office4

REM echo Open office not found neither in C:\Program Files\OpenOffice.org 3 nor in C:\Program Files\OpenOffice 4. If you have it installed in other directory, please setup it in settings.properties
REM goto found

REM :office3
REM echo Open office 3 found!
REM goto :found

REM :office4
REM echo Open office 4 found!

REM :found
echo ===============================================================
"%JAVA_HOME%/bin/java" -Xms512m -Xmx1024m -cp target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar;drivers/truezip-driver-zip-7.3.4.jar org.freeeed.ui.FreeEedUI

:end
