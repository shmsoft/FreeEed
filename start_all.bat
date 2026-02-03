@echo off
setlocal ENABLEDELAYEDEXPANSION

echo ================================
echo Starting FreeEed services (Windows)
echo ================================

REM --------------------------------------------------
REM Always run from the script directory
REM --------------------------------------------------
cd /d "%~dp0"

REM --------------------------------------------------
REM Ensure logs directory exists
REM --------------------------------------------------
if not exist logs (
    mkdir logs
)

REM --------------------------------------------------
REM Clear Tomcat env to avoid conflicts
REM --------------------------------------------------
set CATALINA_HOME=
set CATALINA_BASE=

REM --------------------------------------------------
REM Start Tomcat
REM --------------------------------------------------
echo Starting Tomcat...
cd freeeed-tomcat\bin
start "FreeEed Tomcat" cmd /c startup.bat
cd ..\..

REM --------------------------------------------------
REM Start Solr
REM --------------------------------------------------
echo Starting Solr...
cd freeeed-solr\example
start "FreeEed Solr" cmd /c ^
    java -Xmx1024M -jar start.jar ^
    > ..\..\logs\solr.log 2>&1
cd ..\..

REM --------------------------------------------------
REM Prevent multiple Tika instances
REM --------------------------------------------------
tasklist /FI "IMAGENAME eq java.exe" | find /I "tika-server.jar" >nul
if %ERRORLEVEL%==0 (
    echo Tika already running – skipping
) else (
    echo Starting Tika...
    cd freeeed-tika
    start "FreeEed Tika" cmd /c ^
        java -Xmx1024M -jar tika-server.jar ^
        > ..\logs\tika.log 2>&1
    cd ..
)

REM --------------------------------------------------
REM Start FreeEed Player
REM --------------------------------------------------
echo Starting FreeEed Player...
cd FreeEed
start "FreeEed Player" cmd /c freeeed_player.bat
cd ..

echo ================================
echo All services started
echo ================================

endlocal

