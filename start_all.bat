@echo off
setlocal ENABLEDELAYEDEXPANSION

echo ================================
echo Starting FreeEed services (Windows)
echo ================================

REM --------------------------------------------------
REM Services are launched MINIMIZED (start /min) so their console windows do
REM not clutter the desktop. Solr/Tika use javaw (no console); Tomcat keeps its
REM tested startup.bat (its own window, minimized). See shmsoft/FreeEed#594.
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
REM Tomcat's startup.bat requires JAVA_HOME (or JRE_HOME); plain 'java' on PATH is
REM not enough - without it Tomcat aborts with "JRE_HOME is not defined correctly"
REM and the review app on :8090 never comes up (#594). Derive JAVA_HOME from the
REM java on PATH when it isn't already set.
REM --------------------------------------------------
if not defined JAVA_HOME if not defined JRE_HOME (
    for /f "delims=" %%J in ('where java 2^>nul') do (
        for %%D in ("%%~dpJ..") do set "JAVA_HOME=%%~fD"
    )
)
if defined JAVA_HOME echo Using JAVA_HOME=%JAVA_HOME%

REM --------------------------------------------------
REM Start Tomcat
REM --------------------------------------------------
echo Starting Tomcat...
cd freeeed-tomcat\bin
REM Use the TESTED startup.bat. ("catalina run" failed to start Tomcat on Windows,
REM so the review app on :8090 gave ERR_CONNECTION_REFUSED - #594.) Tomcat opens
REM its own window; /min minimizes our wrapper. A fully-hidden Tomcat is deferred
REM to the NSSM follow-up - working beats hidden.
start "FreeEed Tomcat" /min cmd /c startup.bat
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

