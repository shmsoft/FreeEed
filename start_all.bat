@echo off
setlocal ENABLEDELAYEDEXPANSION

echo ================================
echo Starting FreeEed services (Windows)
echo ================================

REM --------------------------------------------------
REM Services are launched MINIMIZED (start /min) so their console windows do
REM not clutter the desktop. Solr/Tika use javaw (no console); Tomcat uses
REM "catalina run" so it stays in our minimized window instead of spawning its
REM own; all output is redirected to logs\. See shmsoft/FreeEed#594.
REM Follow-up for a fully hidden / true-background experience: run these as
REM Windows services (NSSM) with a small tray status indicator.
REM --------------------------------------------------

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
REM Use "catalina run" (foreground) instead of startup.bat: startup.bat spawns
REM its OWN "Tomcat" console window that /min on our wrapper can't control.
REM catalina run keeps Tomcat inside this minimized window. shutdown.bat (used
REM by stop_all.bat) still stops it via the shutdown port.
start "FreeEed Tomcat" /min cmd /c ^
    catalina.bat run ^
    > ..\..\logs\tomcat.log 2>&1
cd ..\..

REM --------------------------------------------------
REM Start Solr
REM --------------------------------------------------
echo Starting Solr...
cd freeeed-solr\example
start "FreeEed Solr" /min cmd /c ^
    javaw -Xmx1024M -jar start.jar ^
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
    start "FreeEed Tika" /min cmd /c ^
        javaw -Xmx1024M -jar tika-server.jar ^
        > ..\logs\tika.log 2>&1
    cd ..
)

REM --------------------------------------------------
REM Start FreeEed Player
REM --------------------------------------------------
echo Starting FreeEed Player...
cd FreeEed
start "FreeEed Player" /min cmd /c freeeed_player.bat
cd ..

REM --------------------------------------------------
REM Start Python Backend
REM --------------------------------------------------
if exist ..\python (
    echo Starting Python backend...
    cd ..\python
    if exist myenv\Scripts\activate.bat (
        call myenv\Scripts\activate.bat
        start "FreeEed Python Backend" /min cmd /c "python -m uvicorn main:app --reload"
    ) else (
        echo Warning: Python virtual environment not found at ..\python\myenv
    )
    cd ..\freeeed_complete_pack
) else (
    echo Warning: Python directory ..\python not found. Python backend will not start.
)

echo ================================
echo All services started
echo ================================

endlocal

