@echo off
setlocal

echo ================================
echo Stopping FreeEed services (Windows)
echo ================================

REM --------------------------------------------------
REM Always run from this script directory
REM --------------------------------------------------
cd /d "%~dp0"

REM --------------------------------------------------
REM Clear Tomcat env to avoid conflicts
REM --------------------------------------------------
set CATALINA_HOME=
set CATALINA_BASE=

REM --------------------------------------------------
REM Stop Tomcat gracefully
REM --------------------------------------------------
echo Stopping Tomcat...
cd freeeed-tomcat\bin
call shutdown.bat
cd ..\..

REM --------------------------------------------------
REM Show Solr processes (start.jar)
REM --------------------------------------------------
echo.
echo Checking Solr (start.jar) processes:
tasklist /FI "IMAGENAME eq java.exe" | find /I "start.jar"

REM --------------------------------------------------
REM Show Tika processes
REM --------------------------------------------------
echo.
echo Checking Tika processes:
tasklist /FI "IMAGENAME eq java.exe" | find /I "tika"

echo.
echo ================================
echo Stop script completed
echo ================================

endlocal

